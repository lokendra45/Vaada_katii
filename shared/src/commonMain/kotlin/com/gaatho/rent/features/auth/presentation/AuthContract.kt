package com.gaatho.rent.features.auth.presentation

import androidx.compose.runtime.Immutable
import com.gaatho.rent.core.auth.UserRole
import com.gaatho.rent.core.ui.UiState
import kotlinx.serialization.Serializable

/**
 * Represents the immutable UI state for the Passwordless Phone OTP Authentication flow.
 *
 * ## Annotations
 * - `@Immutable` — Tells the Compose compiler all properties are stable.
 * - `@Serializable` — Required by Orbit's saved state mechanism to persist state across process death.
 */
@Serializable
@Immutable
data class AuthState(
    val authUiState: UiState<Unit> = UiState.Idle,
    /** 10-digit Nepal mobile number entered by the user (e.g. "9841234567"). */
    val phoneNumberInput: String = "",
    /** 6-digit verification code entered on the Verify OTP screen (e.g. "012345"). */
    val otpCodeInput: String = "",
    /** Chosen active workspace mode ([UserRole.LANDLORD] vs [UserRole.TENANT]). */
    val selectedRole: UserRole = UserRole.LANDLORD,
    /** Seconds remaining before user can request a new OTP code. */
    val resendTimerSeconds: Int = 60
) {
    val canResend: Boolean
        get() = resendTimerSeconds <= 0 && authUiState !is UiState.Loading

    val isVerifyEnabled: Boolean
        get() = otpCodeInput.length == 6 && authUiState !is UiState.Loading

    val isContinueEnabled: Boolean
        get() = phoneNumberInput.length == 10 && authUiState !is UiState.Loading
}

/**
 * One-time side effects for the Phone OTP screens.
 * Backed by Orbit's side-effect Channel for exact-once delivery to the UI.
 */
sealed interface AuthSideEffect {
    /** Navigate to the main Dashboard screen (`PropertyListRoute`) after successful OTP verification. */
    data object NavigateToHome : AuthSideEffect

    /** Navigate from [PhoneOtpLoginScreen] to [VerifyOtpScreen] once OTP has been dispatched. */
    data class NavigateToVerifyOtp(val phoneNumber: String, val selectedRole: UserRole) : AuthSideEffect

    /** Show a Snackbar or inline error message. */
    data class ShowError(val message: String) : AuthSideEffect

    /** Show a Snackbar or toast confirmation (e.g. "Code resent to +977 9841234567"). */
    data class ShowSuccess(val message: String) : AuthSideEffect
}

/**
 * All possible user interactions across the Phone OTP screens.
 */
sealed interface AuthAction {
    /** User types or edits their 10-digit Nepal mobile number. */
    data class OnPhoneChanged(val phone: String) : AuthAction

    /** User types or edits their 6-digit OTP verification code. */
    data class OnOtpChanged(val otp: String) : AuthAction

    /** User selects the Landlord or Tenant tab. */
    data class OnRoleSelected(val role: UserRole) : AuthAction

    /** User taps "Send Verification Code" on [PhoneOtpLoginScreen]. */
    data object OnSendPhoneOtpClicked : AuthAction

    /** User taps "Verify & Continue" on [VerifyOtpScreen]. */
    data class OnVerifyPhoneOtpClicked(val phone: String, val role: UserRole) : AuthAction

    /** User taps "Resend Code" on [VerifyOtpScreen]. */
    data class OnResendOtpClicked(val phone: String, val role: UserRole) : AuthAction

    /** Start or reset the 60s resend countdown timer inside the ViewModel. */
    data object OnStartResendTimer : AuthAction
}
