package com.gaatho.rent.features.splash.presentation

import com.gaatho.rent.core.auth.AuthState
import com.gaatho.rent.core.auth.AuthDeepLinkFlags
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.mvi.MviViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

private const val SPLASH_SETTLE_TIMEOUT_MS = 10_000L
private const val DEEPLINK_GRACE_MS = 2_500L

/**
 * Splash screen ViewModel.
 *
 * Waits for Supabase Auth to settle, then routes:
 * - Authenticated / Anonymous → Home
 * - Unauthenticated → Login
 *
 * Does NOT auto-provision anonymous sessions. That is the Login screen's
 * responsibility via "Continue as Guest". This eliminates the race condition
 * where OAuth deep-link processing hasn't completed yet and the splash
 * would wrongly provision a new anonymous session.
 */
class SplashViewModel(
    private val sessionManager: SessionManager,
) : MviViewModel<SplashState, SplashSideEffect, SplashAction>() {

    override val container = orbitContainer<SplashState, SplashSideEffect>(SplashState()) {
        initialize()
    }

    override fun onAction(action: SplashAction) {
        when (action) {
            is SplashAction.Retry -> retry()
        }
    }

    private fun initialize() = intent {
        reduce { state.copy(phase = SplashState.Phase.Loading) }
        val mark = TimeSource.Monotonic.markNow()

        try {
            AppLogger.auth.i { "Splash: waiting for AuthState to settle..." }

            // Guard against a session status that never leaves Loading (e.g. a stuck
            // storage read) so the splash can never hang forever.
            val settled = withTimeoutOrNull(SPLASH_SETTLE_TIMEOUT_MS) {
                sessionManager.authState.first { it !is AuthState.Loading }
            }
            var finalState = settled ?: AuthState.Unauthenticated

            // If the app was launched via the OAuth redirect deep link, a PKCE code
            // exchange may still be in flight. Wait a bounded amount for it to complete
            // before defaulting to Login — but ONLY in that case, to avoid slowing down
            // ordinary cold starts.
            if (finalState is AuthState.Unauthenticated && AuthDeepLinkFlags.pendingOAuth) {
                AppLogger.auth.i { "Splash: pending OAuth deep link — waiting for exchange" }
                finalState = withTimeoutOrNull(DEEPLINK_GRACE_MS) {
                    sessionManager.authState.first {
                        it is AuthState.Authenticated || it is AuthState.Anonymous
                    }
                } ?: finalState
            }
            AuthDeepLinkFlags.pendingOAuth = false

            AppLogger.auth.i { "Splash: settled on ${finalState::class.simpleName}" }

            // Enforce minimum brand display time
            val elapsed = mark.elapsedNow()
            val minDisplay = 1_400.milliseconds
            if (elapsed < minDisplay) {
                kotlinx.coroutines.delay((minDisplay - elapsed).inWholeMilliseconds)
            }

            when (finalState) {
                is AuthState.Authenticated -> {
                    AppLogger.auth.i { "Splash → Home (authenticated: ${finalState.user.email})" }
                    postSideEffect(SplashSideEffect.NavigateToHome)
                }
                is AuthState.Anonymous -> {
                    AppLogger.auth.i { "Splash → Home (anonymous guest: ${finalState.user.id})" }
                    postSideEffect(SplashSideEffect.NavigateToHome)
                }
                is AuthState.Unauthenticated -> {
                    AppLogger.auth.i { "Splash → Login (no session)" }
                    postSideEffect(SplashSideEffect.NavigateToLogin)
                }
                is AuthState.Loading -> {
                    // Unreachable due to .first filter
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
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
}
