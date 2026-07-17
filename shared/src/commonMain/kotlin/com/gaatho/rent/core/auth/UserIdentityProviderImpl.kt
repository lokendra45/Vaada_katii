package com.gaatho.rent.core.auth

/**
 * Implementation of [UserIdentityProvider] that delegates to [SessionManager] for remote
 * Supabase accounts and falls back to [GuestSessionManager] for offline guest mode.
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
        return guestSessionManager.getOrCreateGuestId()
    }

    override fun isGuest(): Boolean {
        return !sessionManager.isLoggedIn.value
    }
}
