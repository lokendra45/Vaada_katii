package com.gaatho.rent.features.splash.presentation

/**
 * State representation for the Splash screen.
 */
data class SplashState(
    val isLoading: Boolean = true
)

/**
 * One-shot side effects emitted by the Splash screen.
 *
 * The app always navigates to Home after splash.
 * Login is only triggered later by the user via an explicit action (e.g. "Sign In").
 */
sealed interface SplashSideEffect {
    /** Navigate to the main dashboard. Always fires — guest session is auto-created if needed. */
    data object NavigateToHome : SplashSideEffect
}

