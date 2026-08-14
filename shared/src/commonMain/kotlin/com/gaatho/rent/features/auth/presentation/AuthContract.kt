package com.gaatho.rent.features.auth.presentation

import com.gaatho.rent.core.auth.UserRole
import com.gaatho.rent.core.ui.UiState
import kotlinx.serialization.Serializable

@Serializable
data class AuthState(
    val authUiState: UiState<Unit> = UiState.Idle,
    val emailInput: String = "",
    val passwordInput: String = "",
    val isLoginMode: Boolean = true,
    val selectedRole: UserRole = UserRole.LANDLORD
) {
    val isEmailValid: Boolean
        get() = emailInput.isNotBlank() && emailInput.contains("@")

    val isPasswordValid: Boolean
        get() = passwordInput.isNotBlank() && passwordInput.length >= 6

    val isSubmitEnabled: Boolean
        get() = isEmailValid && isPasswordValid && authUiState !is UiState.Loading
}

sealed interface AuthSideEffect {
    data object NavigateToHome : AuthSideEffect
    data class ShowError(val message: String) : AuthSideEffect
    data class ShowSuccess(val message: String) : AuthSideEffect
}

sealed interface AuthAction {
    data class OnEmailChanged(val email: String) : AuthAction
    data class OnPasswordChanged(val password: String) : AuthAction
    data class OnRoleSelected(val role: UserRole) : AuthAction
    data object OnToggleAuthMode : AuthAction
    data object OnSubmitEmailAuthClicked : AuthAction
    data object OnGoogleAuthClicked : AuthAction
    data object OnGuestAuthClicked : AuthAction
}
