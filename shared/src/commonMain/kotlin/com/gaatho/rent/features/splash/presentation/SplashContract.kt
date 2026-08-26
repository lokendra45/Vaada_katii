package com.gaatho.rent.features.splash.presentation

/**
 * UI state for the Splash screen.
 *
 * The screen is always in exactly one of three phases:
 * - [Phase.Loading]  → initial session check is in progress.
 * - [Phase.Error]    → a non-recoverable network or auth error occurred.
 *   The user can tap "Try Again" to re-run [SplashViewModel.retry].
 * - (Navigating)     → [SplashSideEffect] fires; state stays Loading until nav completes.
 */
data class SplashState(
    val phase: Phase = Phase.Loading,
) {
    sealed interface Phase {
        data object Loading : Phase
        data class Error(val message: String) : Phase
    }
}

/**
 * One-shot navigation side effects emitted by [SplashViewModel].
 */
sealed interface SplashSideEffect {
    /**
     * Navigate to the main dashboard.
     */
    data object NavigateToHome : SplashSideEffect

    /**
     * Navigate to the login screen.
     * Emitted when no existing session (authenticated or anonymous) is found.
     */
    data object NavigateToLogin : SplashSideEffect
}
