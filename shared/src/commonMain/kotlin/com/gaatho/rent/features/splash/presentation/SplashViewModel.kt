package com.gaatho.rent.features.splash.presentation

import androidx.lifecycle.ViewModel
import com.gaatho.rent.core.auth.GuestSessionManager
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.logging.AppLogger
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the Splash screen.
 * Responsible for verifying session validity and initial startup data checks,
 * then directing the user to the appropriate destination ([SplashSideEffect.NavigateToHome]
 * or [SplashSideEffect.NavigateToLogin]).
 */
class SplashViewModel(
    private val sessionManager: SessionManager,
    private val guestSessionManager: GuestSessionManager
) : ContainerHost<SplashState, SplashSideEffect>, ViewModel() {

    override val container = orbitContainer<SplashState, SplashSideEffect>(SplashState()) {
        checkSessionAndNavigate()
    }

    private fun checkSessionAndNavigate() = intent {
        AppLogger.ui.i { "Initializing startup checks on Splash Screen..." }
        
        // Brief minimum display time so the rich branded splash experience feels polished
        // rather than jarringly flashing across the screen.
        delay(1200.milliseconds)

        val isLoggedIn = sessionManager.isLoggedIn.value
        val isGuest = guestSessionManager.hasActiveGuestSession()
        AppLogger.auth.i { "Session validation complete. IsLoggedIn = $isLoggedIn, IsGuest = $isGuest" }

        if (isLoggedIn || isGuest) {
            postSideEffect(SplashSideEffect.NavigateToHome)
        } else {
            postSideEffect(SplashSideEffect.NavigateToLogin)
        }
    }
}
