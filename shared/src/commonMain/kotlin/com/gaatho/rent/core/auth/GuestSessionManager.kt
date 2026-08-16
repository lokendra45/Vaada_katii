package com.gaatho.rent.core.auth

/**
 * Manages the guest (anonymous) session for users who explore the app without
 * creating an account.
 *
 * With the Supabase-only architecture, a guest is an anonymous Supabase user
 * (`auth.signInAnonymously()`). They get a real `auth.uid()` so that PostgREST
 * row-level security (`owner_id = auth.uid()::text`) applies to guest data just
 * like paid users. The guest-mode flag is persisted in DataStore.
 */
interface GuestSessionManager {

    /**
     * Ensures an anonymous Supabase session exists for guest mode and returns the
     * guest's Supabase uid. Idempotent — reuses an existing session when present.
     * Marks guest mode as active in DataStore.
     */
    suspend fun ensureGuestSession(): String

    /**
     * Returns `true` if guest mode is active (an anonymous session was created).
     */
    fun hasActiveGuestSession(): Boolean

    /**
     * Clears the guest-mode flag. Does not sign out the underlying Supabase session —
     * that is handled by [AuthRepository.signOut].
     */
    suspend fun clearGuestSession()
}