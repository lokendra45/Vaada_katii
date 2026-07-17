package com.gaatho.rent.core.auth

/**
 * Manages a local guest session for unauthenticated users.
 *
 * A guest identity is a stable UUID persisted in the local SQLDelight database.
 * It requires zero network contact and is completely independent of Supabase Auth.
 *
 * ## Separation of Concerns
 * This is a `core` concern. It does NOT know about:
 * - Feature modules (property, paywall, etc.)
 * - Supabase or any remote service
 * - RevenueCat or subscription status
 */
interface GuestSessionManager {
    /**
     * Returns the existing guest UUID, or generates and persists a new one.
     * Idempotent — always returns the same UUID for a given device installation.
     */
    fun getOrCreateGuestId(): String

    /**
     * Clears the local guest UUID.
     * Called when the guest creates a real account or signs out.
     */
    fun clearGuestSession()

    /**
     * Returns true if there is currently no Supabase session and a local guest ID exists.
     */
    fun hasActiveGuestSession(): Boolean
}
