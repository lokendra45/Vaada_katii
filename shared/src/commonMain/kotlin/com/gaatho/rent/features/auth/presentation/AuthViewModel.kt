package com.gaatho.rent.features.auth.presentation

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.auth.AuthRepository
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.CancellationException
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
            is AuthAction.OnGoogleAuthClicked -> handleBrowserGoogleSignIn()
            is AuthAction.OnGoogleSignInSuccess -> intent {
                // Navigation to Home is driven reactively by authState in the Login screen.
                reduce { state.copy(authUiState = UiState.Success(Unit)) }
            }
            is AuthAction.OnTrySeamlessSignIn -> intent { 
                // Handled natively by composeAuth if desired, no-op here
            }
            is AuthAction.OnGoogleSignInError -> intent {
                reduce { state.copy(authUiState = UiState.Error(action.message)) }
                postSideEffect(AuthSideEffect.ShowError(action.message))
            }
            is AuthAction.OnGuestAuthClicked -> handleGuestAuth()
            is AuthAction.OnEnsureRole -> intent {
                // Stamp the chosen role for new social sign-ins (Google) that cannot
                // carry metadata at sign-in time. Best-effort; never blocks navigation.
                try {
                    authRepository.ensureUserRole(state.selectedRole)
                } catch (_: Throwable) {
                }
            }
        }
    }

    private fun handleEmailAuth() = intent {
        if (!state.isSubmitEnabled) return@intent
        reduce { state.copy(authUiState = UiState.Loading) }

        try {
            val email = state.emailInput.trim()
            val password = state.passwordInput.trim()

            if (state.isLoginMode) {
                when (val response = authRepository.signInWithEmail(email, password)) {
                    is ApiResponse.Success -> {
                        // Navigation to Home is driven reactively by authState in the Login screen.
                        reduce { state.copy(authUiState = UiState.Success(Unit)) }
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
            } else {
                when (val response = authRepository.signUpWithEmail(email, password, state.selectedRole)) {
                    is ApiResponse.Success -> {
                        if (response.data == true) {
                            // Auto-confirm enabled — session exists, reactive nav will fire.
                            reduce { state.copy(authUiState = UiState.Success(Unit)) }
                        } else {
                            // Email confirmation required: no session yet. Stay on the login
                            // screen and tell the user to verify their inbox. The reactive
                            // observer won't navigate because authState stays Unauthenticated.
                            reduce { state.copy(authUiState = UiState.Idle) }
                            postSideEffect(AuthSideEffect.ShowSuccess("Check your email to verify your account."))
                        }
                    }
                    is ApiResponse.Failure.Error -> {
                        val errorMsg = ErrorMessageExtractor.extract(response, "Sign-up failed.")
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val errorMsg = ErrorMessageExtractor.extract(e, "Authentication failed.")
            reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
            postSideEffect(AuthSideEffect.ShowError(errorMsg))
        }
    }

    private fun handleBrowserGoogleSignIn() = intent {
        // Browser OAuth only *launches* the system browser; the real session arrives
        // asynchronously via the OAuth deep link (supabase.handleDeeplinks). Navigation to Home is
        // driven reactively by authState in the Login screen. We do NOT put the UI into a Loading
        // state here: the browser is a full-screen takeover (a spinner would be invisible anyway),
        // and leaving the UI idle means a cancelled OAuth simply returns to a usable login screen
        // with no stuck spinner and no timer-based recovery needed.
        val response = authRepository.signInWithGoogle(role = state.selectedRole)
        when (response) {
            is ApiResponse.Success -> {
                // No-op: await the session via the reactive authState observer in LoginScreen.
            }
            is ApiResponse.Failure.Error -> {
                val errorMsg = ErrorMessageExtractor.extract(response, "Google sign-in failed.")
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
            is ApiResponse.Failure.Exception -> {
                val errorMsg = ErrorMessageExtractor.extract(response.throwable, "Network error.")
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
        }
    }

    private fun handleGuestAuth() = intent {
        reduce { state.copy(authUiState = UiState.Loading) }
        val response = authRepository.signInAnonymously()
        when (response) {
            is ApiResponse.Success<*> -> {
                AppLogger.auth.i { "Continued as anonymous guest" }
                // Navigation to Home is driven reactively by authState in the Login screen.
                reduce { state.copy(authUiState = UiState.Success(Unit)) }
            }
            is ApiResponse.Failure.Error -> {
                val errorMsg = ErrorMessageExtractor.extract(response, "Couldn't start a guest session.")
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
            is ApiResponse.Failure.Exception -> {
                val errorMsg = ErrorMessageExtractor.extract(response.throwable, "Network error.")
                reduce { state.copy(authUiState = UiState.Error(errorMsg)) }
                postSideEffect(AuthSideEffect.ShowError(errorMsg))
            }
        }
    }
}
