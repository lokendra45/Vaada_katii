package com.gaatho.rent.features.auth.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gaatho.rent.core.auth.AuthRepository
import com.gaatho.rent.core.auth.UserRole
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.viewmodel.orbitContainer

/**
 * ViewModel responsible for the Passwordless Phone OTP Authentication flow.
 *
 * Exactly mirrors GaathoMobileApp's LoginViewModel and OtpViewModel logic, owning
 * all countdown timers, validation, and network calls so that UI screens are 100% stateless.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : MviViewModel<AuthState, AuthSideEffect, AuthAction>() {

    override val container = orbitContainer<AuthState, AuthSideEffect>(
        initialState = AuthState(),
        savedStateHandle = savedStateHandle,
        serializer = AuthState.serializer()
    )

    private var timerJob: Job? = null

    override fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.OnPhoneChanged -> intent {
                reduce {
                    state.copy(
                        phoneNumberInput = action.phone,
                        authUiState = UiState.Idle
                    )
                }
            }
            is AuthAction.OnOtpChanged -> intent {
                reduce {
                    state.copy(
                        otpCodeInput = action.otp,
                        authUiState = UiState.Idle
                    )
                }
            }
            is AuthAction.OnRoleSelected -> intent {
                reduce { state.copy(selectedRole = action.role) }
            }
            is AuthAction.OnSendPhoneOtpClicked -> handleSendPhoneOtp()
            is AuthAction.OnVerifyPhoneOtpClicked -> handleVerifyPhoneOtp(action.phone, action.role)
            is AuthAction.OnResendOtpClicked -> handleResendOtp(action.phone, action.role)
            is AuthAction.OnStartResendTimer -> startResendTimer()
        }
    }

    private fun handleSendPhoneOtp() = intent {
        val cleanDigits = state.phoneNumberInput.trim().filter { it.isDigit() }
        if (cleanDigits.length != 10) {
            postSideEffect(AuthSideEffect.ShowError("Please enter a valid 10-digit Nepal mobile number (e.g. 9841234567)."))
            return@intent
        }

        val formattedPhone = "+977$cleanDigits"
        reduce { state.copy(authUiState = UiState.Loading) }
        AppLogger.auth.i { "Requesting OTP verification code for $formattedPhone as ${state.selectedRole}..." }

        when (val result = authRepository.signInWithPhoneOtp(
            phone = formattedPhone,
            role = state.selectedRole
        )) {
            is ApiResponse.Success -> {
                AppLogger.auth.i { "OTP dispatched successfully to $formattedPhone." }
                reduce { state.copy(authUiState = UiState.Success(Unit)) }
                postSideEffect(AuthSideEffect.ShowSuccess("Verification code sent to $formattedPhone!"))
                postSideEffect(AuthSideEffect.NavigateToVerifyOtp(formattedPhone, state.selectedRole))
                startResendTimer()
            }
            is ApiResponse.Failure.Error -> {
                val errorMsg = ErrorMessageExtractor.extract(result, "Failed to send verification code. Check phone number format.")
                AppLogger.auth.e { "OTP Dispatch Error: $errorMsg" }
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
            is ApiResponse.Failure.Exception -> {
                val errorMsg = ErrorMessageExtractor.extract(result.throwable, "Network connection error. Please try again.")
                AppLogger.auth.e(result.throwable) { "OTP Dispatch Exception: $errorMsg" }
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
        }
    }

    private fun handleVerifyPhoneOtp(phone: String, role: UserRole) = intent {
        val cleanOtp = state.otpCodeInput.trim().filter { it.isDigit() }
        if (cleanOtp.length != 6) {
            postSideEffect(AuthSideEffect.ShowError("Please enter the 6-digit verification code."))
            return@intent
        }

        reduce { state.copy(authUiState = UiState.Loading) }
        AppLogger.auth.i { "Verifying OTP code for $phone..." }

        when (val result = authRepository.verifyPhoneOtp(
            phone = phone,
            token = cleanOtp
        )) {
            is ApiResponse.Success -> {
                AppLogger.auth.i { "OTP verified successfully for $phone! Session established." }
                timerJob?.cancel()
                reduce { state.copy(authUiState = UiState.Success(Unit)) }
                postSideEffect(AuthSideEffect.ShowSuccess("Welcome to Rent Manager Nepal!"))
                postSideEffect(AuthSideEffect.NavigateToHome)
            }
            is ApiResponse.Failure.Error -> {
                val errorMsg = ErrorMessageExtractor.extract(result, "Incorrect verification code. Please check and try again.")
                AppLogger.auth.e { "OTP Verify Error: $errorMsg" }
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
            is ApiResponse.Failure.Exception -> {
                val errorMsg = ErrorMessageExtractor.extract(result.throwable, "Network error during verification. Please check internet connection.")
                AppLogger.auth.e(result.throwable) { "OTP Verify Exception: $errorMsg" }
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
        }
    }

    private fun handleResendOtp(phone: String, role: UserRole) = intent {
        if (!state.canResend) return@intent

        reduce { state.copy(resendTimerSeconds = 60, authUiState = UiState.Loading) }
        AppLogger.auth.i { "Resending verification code to $phone..." }

        when (val result = authRepository.signInWithPhoneOtp(phone, role)) {
            is ApiResponse.Success -> {
                reduce { state.copy(authUiState = UiState.Success(Unit)) }
                postSideEffect(AuthSideEffect.ShowSuccess("New code sent to $phone!"))
                startResendTimer()
            }
            is ApiResponse.Failure.Error -> {
                val errorMsg = ErrorMessageExtractor.extract(result, "Could not resend verification code right now.")
                reduce { state.copy(resendTimerSeconds = 0, authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
            is ApiResponse.Failure.Exception -> {
                val errorMsg = ErrorMessageExtractor.extract(result.throwable, "Network connection error. Please try again.")
                reduce { state.copy(resendTimerSeconds = 0, authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            intent {
                reduce { state.copy(resendTimerSeconds = 60) }
            }
            while (container.stateFlow.value.resendTimerSeconds > 0) {
                delay(1000)
                intent {
                    reduce {
                        val newSeconds = state.resendTimerSeconds - 1
                        state.copy(resendTimerSeconds = newSeconds)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
