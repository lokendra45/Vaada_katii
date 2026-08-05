package com.gaatho.rent.features.splash.presentation

import com.gaatho.rent.core.auth.GuestSessionManager
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.mvi.MviViewModel
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * ViewModel for the Splash screen.
 *
 * ## Flow
 * 1. Record the start time.
 * 2. Do real work (check session / ensure a guest identity exists).
 * 3. Wait for the *remainder* of the minimum brand display window (1 200 ms),
 *    so we never artificially pad startup beyond what's already elapsed.
 * 4. Always emit [SplashSideEffect.NavigateToHome] — login is triggered
 *    later by an explicit user action (e.g. "Sign In" in Settings).
 *
 * ## No hardcoded `delay` for production padding
 * Using [TimeSource] ensures the splash exits as soon as real work finishes
 * *and* the minimum brand window has elapsed — whichever is longer.
 */
class SplashViewModel(
    private val sessionManager: SessionManager,
    private val guestSessionManager: GuestSessionManager
) : MviViewModel<SplashState, SplashSideEffect, SplashAction>() {

    override val container = orbitContainer<SplashState, SplashSideEffect>(SplashState()) {
        initializeSession()
    }

    private fun initializeSession() = intent {
        val mark = TimeSource.Monotonic.markNow()

        // --- Real work (runs immediately, no artificial pause) ---
        val isLoggedIn = sessionManager.isLoggedIn.value
        AppLogger.auth.i { "Session check complete. isLoggedIn=$isLoggedIn" }

        if (!isLoggedIn) {
            // Guarantee a stable local identity so all features work offline/guest.
            val guestId = guestSessionManager.getOrCreateGuestId()
            AppLogger.auth.i { "Guest session ready. guestId=$guestId" }
        }

        // --- Brand window: wait only for the *remaining* time ---
        // This keeps the splash visually polished without a fixed blocking delay.
        val minimumDisplayMs = 1_200.milliseconds
        val elapsed = mark.elapsedNow()
        if (elapsed < minimumDisplayMs) {
            delay(minimumDisplayMs - elapsed)
        }

        postSideEffect(SplashSideEffect.NavigateToHome)
    }

    override fun onAction(action: SplashAction) {}
}

