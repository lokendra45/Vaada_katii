package com.gaatho.rent.core.auth

/**
 * Implementation of [UserIdentityProvider] that delegates to [SessionManager] for remote
 * Supabase accounts (including anonymous guest sessions) and falls back to [GuestSessionManager]
 * for the guest-mode flag.
 */
class UserIdentityProviderImpl(
    private val sessionManager: SessionManager,
    private val guestSessionManager: GuestSessionManager
) : UserIdentityProvider {

    override fun currentUserId(): String {
        val remoteId = sessionManager.currentUserId()
        if (remoteId != null) {
            return remoteId
        }
        return ""
    }

    override fun isGuest(): Boolean {
        val user = sessionManager.currentUser.value
        if (user != null) {
            return user.isAnonymous
        }
        return guestSessionManager.hasActiveGuestSession()
    }
}