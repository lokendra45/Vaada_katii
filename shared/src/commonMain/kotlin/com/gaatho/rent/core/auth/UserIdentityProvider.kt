package com.gaatho.rent.core.auth

/**
 * Single source of truth for the active user's identity across the application.
 *
 * Provides a clean abstraction over whether the current user is an authenticated
 * Supabase account holder or an anonymous guest session.
 *
 * ## Decoupled Design
 * Feature ViewModels depend on this interface rather than directly coupling to
 * [io.github.jan.supabase.SupabaseClient] or [GuestSessionManager].
 */
interface UserIdentityProvider {
    /**
     * Returns the active user ID (`owner_id`).
     * - If signed in via Supabase: returns the Supabase user UUID (`auth.users.id`).
     * - If in Guest mode: returns the anonymous Supabase user UUID.
     * - If no session exists: returns an empty string (data reads simply return empty).
     */
    fun currentUserId(): String

    /**
     * Returns `true` if the current user is operating as an anonymous guest session
     * (i.e. not authenticated with a real Supabase account).
     */
    fun isGuest(): Boolean
}
