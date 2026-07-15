package com.gaatho.rent.features.splash.presentation

/**
 * State representation for the Splash screen.
 */
data class SplashState(
    val isLoading: Boolean = true
)

/**
 * One-shot side effects emitted by the Splash screen to trigger navigation after initial validation.
 */
sealed interface SplashSideEffect {
    /** Navigate directly to the main dashboard (user has a valid session). */
    data object NavigateToHome : SplashSideEffect

    /** Navigate to the login screen (no valid session or session expired). */
    data object NavigateToLogin : SplashSideEffect
}
