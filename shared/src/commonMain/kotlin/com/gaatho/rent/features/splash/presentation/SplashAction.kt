package com.gaatho.rent.features.splash.presentation

sealed interface SplashAction {
    /** Triggered by the user tapping "Try Again" when [SplashState.Phase.Error] is shown. */
    data object Retry : SplashAction
}
