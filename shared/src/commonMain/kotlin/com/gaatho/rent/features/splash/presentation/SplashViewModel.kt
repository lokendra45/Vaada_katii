package com.gaatho.rent.features.splash.presentation

import com.gaatho.rent.core.auth.GuestSessionManager
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.mvi.MviViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Production-grade ViewModel for the Splash screen.
 *
 * ## Responsibilities
 * 1. **Await** the Supabase session to be non-null before proceeding — eliminates the
 *    race condition where [SessionManager.currentUserId] returns "" on cold start.
 * 2. **Provision guest** — if no real account is logged in, create/reuse an anonymous
 *    Supabase session so that RLS (`owner_id = auth.uid()`) always has a valid UUID.
 * 3. **Enforce brand window** — exit splash only after at least 1 400 ms of visible
 *    branding, using elapsed-time math instead of a fixed delay.
 * 4. **Retry on error** — expose [SplashState.Phase.Error] and allow the UI to retry.
 */
class SplashViewModel(
    private val sessionManager: SessionManager,
    private val guestSessionManager: GuestSessionManager,
) : MviViewModel<SplashState, SplashSideEffect, SplashAction>() {

    override val container = orbitContainer<SplashState, SplashSideEffect>(SplashState()) {
        initialize()
    }

    // ── Public API ────────────────────────────────────────────────────────────

    override fun onAction(action: SplashAction) {
        when (action) {
            is SplashAction.Retry -> retry()
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun initialize() = intent {
        reduce { state.copy(phase = SplashState.Phase.Loading) }
        val mark = TimeSource.Monotonic.markNow()

        try {
            val ownerId = resolveOwnerId()
            AppLogger.auth.i { "Session resolved. ownerId=$ownerId" }

            // Brand window: wait only for remaining time (never blocks if work took longer)
            val elapsed = mark.elapsedNow()
            val minDisplay = 1_400.milliseconds
            if (elapsed < minDisplay) {
                kotlinx.coroutines.delay((minDisplay - elapsed).inWholeMilliseconds)
            }

            val isFirstLaunch = guestSessionManager.hasActiveGuestSession()
            postSideEffect(SplashSideEffect.NavigateToHome(isFirstLaunch = isFirstLaunch))

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.auth.e(e) { "Splash initialization failed" }
            reduce {
                state.copy(phase = SplashState.Phase.Error(e.message ?: "Unknown error"))
            }
        }
    }

    private fun retry() = intent {
        initialize()
    }

    /**
     * Waits (suspends) for the Supabase session to settle, then returns the user ID.
     * If no real account exists, provisions an anonymous guest session and returns its ID.
     *
     * Using `.first { ... }` ensures we never read the ID synchronously during cold start
     * before the local token cache has been restored by the Supabase SDK.
     */
    private suspend fun resolveOwnerId(): String {
        val existingUser = sessionManager.currentUser
            .first { it != null || !sessionManager.isLoggedIn.value }

        if (existingUser != null) {
            AppLogger.auth.i { "isLoggedIn=true uid=${existingUser.id}" }
            return existingUser.id
        }

        AppLogger.auth.i { "No session found. Provisioning anonymous guest…" }
        val guestId = guestSessionManager.ensureGuestSession()
        AppLogger.auth.i { "Guest session ready. guestId=$guestId" }
        return guestId
    }
}
