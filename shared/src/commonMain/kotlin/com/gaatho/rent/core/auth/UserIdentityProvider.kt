package com.gaatho.rent.core.auth

/**
 * Single source of truth for the active user's identity across the application.
 *
 * Provides a clean abstraction over whether the current user is an authenticated
 * Supabase account holder or an offline Guest using local storage.
 *
 * ## Decoupled Design
 * Repositories ([ProxyPropertyRepository], [LocalPropertyRepository]) and feature
 * ViewModels depend on this interface rather than directly coupling to [io.github.jan.supabase.SupabaseClient]
 * or [GuestSessionManager].
 */
interface UserIdentityProvider {
    /**
     * Returns the active user ID (`owner_id`).
     * - If signed in via Supabase: returns the Supabase user UUID (`auth.users.id`).
     * - If in Guest mode: returns the stable local guest UUID (`guest_xxx`).
     * - If neither: initializes or returns a local guest UUID as fallback.
     */
    fun currentUserId(): String

    /**
     * Returns `true` if the current user is operating in offline Guest mode
     * (i.e. not authenticated with a remote Supabase account).
     */
    fun isGuest(): Boolean
}
