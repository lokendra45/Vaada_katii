package com.gaatho.rent.core.auth

/**
 * Cross-module flag set by the Android entry point ([MainActivity]) when the app is launched
 * (or resumed) via the OAuth redirect deep link (`com.gaatho.rent://login-callback`).
 *
 * The Splash screen reads this to decide whether it should wait a little longer for a pending
 * PKCE code exchange to complete before routing to Login. It is intentionally not a stable signal
 * of success — only that an OAuth redirect is in flight.
 */
object AuthDeepLinkFlags {
    var pendingOAuth: Boolean = false
}
