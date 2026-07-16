package com.gaatho.rent.features.auth.presentation

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.auth.AuthRepository
import com.gaatho.rent.core.auth.UserRole
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.skydoves.sandwich.ApiResponse
import org.orbitmvi.orbit.viewmodel.orbitContainer

class AuthViewModel(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : MviViewModel<AuthState, AuthSideEffect, AuthAction>() {

    override val container = orbitContainer<AuthState, AuthSideEffect>(
        initialState = AuthState(),
        savedStateHandle = savedStateHandle,
        serializer = AuthState.serializer()
    )

    override fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.OnEmailChanged -> intent {
                reduce { state.copy(emailInput = action.email, authUiState = UiState.Idle) }
            }
            is AuthAction.OnPasswordChanged -> intent {
                reduce { state.copy(passwordInput = action.password, authUiState = UiState.Idle) }
            }
            is AuthAction.OnRoleSelected -> intent {
                reduce { state.copy(selectedRole = action.role) }
            }
            is AuthAction.OnToggleAuthMode -> intent {
                reduce { state.copy(isLoginMode = !state.isLoginMode, authUiState = UiState.Idle) }
            }
            is AuthAction.OnSubmitEmailAuthClicked -> handleEmailAuth()
            is AuthAction.OnGoogleAuthClicked -> handleGoogleAuth()
            is AuthAction.OnGuestAuthClicked -> handleGuestAuth()
        }
    }

    private fun handleEmailAuth() = intent {
        if (!state.isSubmitEnabled) return@intent
        reduce { state.copy(authUiState = UiState.Loading) }

        val email = state.emailInput.trim()
        val password = state.passwordInput.trim()

        val response = if (state.isLoginMode) {
            authRepository.signInWithEmail(email, password)
        } else {
            authRepository.signUpWithEmail(email, password, state.selectedRole)
        }

        when (response) {
            is ApiResponse.Success -> {
                reduce { state.copy(authUiState = UiState.Success(Unit)) }
                postSideEffect(AuthSideEffect.NavigateToHome)
            }
            is ApiResponse.Failure.Error -> {
                val errorMsg = ErrorMessageExtractor.extract(response, "Authentication failed.")
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
            is ApiResponse.Failure.Exception -> {
                val errorMsg = ErrorMessageExtractor.extract(response.throwable, "Network error. Please try again.")
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
        }
    }

    private fun handleGoogleAuth() = intent {
        reduce { state.copy(authUiState = UiState.Loading) }
        when (val response = authRepository.signInWithGoogle()) {
            is ApiResponse.Success -> {
                reduce { state.copy(authUiState = UiState.Success(Unit)) }
                postSideEffect(AuthSideEffect.NavigateToHome)
            }
            is ApiResponse.Failure.Error -> {
                val errorMsg = ErrorMessageExtractor.extract(response, "Google sign-in failed.")
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
            is ApiResponse.Failure.Exception -> {
                val errorMsg = ErrorMessageExtractor.extract(response.throwable, "Network error during Google sign-in.")
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
        }
    }

    private fun handleGuestAuth() = intent {
        reduce { state.copy(authUiState = UiState.Loading) }
        when (val response = authRepository.signInAnonymously()) {
            is ApiResponse.Success -> {
                reduce { state.copy(authUiState = UiState.Success(Unit)) }
                postSideEffect(AuthSideEffect.NavigateToHome)
            }
            is ApiResponse.Failure.Error -> {
                val errorMsg = ErrorMessageExtractor.extract(response, "Guest sign-in failed.")
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
            is ApiResponse.Failure.Exception -> {
                val errorMsg = ErrorMessageExtractor.extract(response.throwable, "Network error during Guest sign-in.")
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
        }
    }
}
