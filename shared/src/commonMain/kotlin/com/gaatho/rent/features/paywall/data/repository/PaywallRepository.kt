package com.gaatho.rent.features.paywall.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Abstraction for checking and updating the user's premium subscription status.
 *
 * This will be backed by a WebView-based payment flow (eSewa / Khalti / etc.)
 * once the payment gateway integration is implemented.
 */
interface PaywallRepository {
    /**
     * Reactive stream of the current user's premium status.
     * Emits `true` when an active subscription has been verified.
     */
    val isPremium: Flow<Boolean>

    /**
     * Manually mark the user as premium after a successful payment callback.
     * Call this from the WebView result handler once the payment gateway
     * confirms a successful transaction.
     */
    suspend fun grantPremiumAccess()

    /**
     * Revoke premium access (e.g. on sign-out or subscription expiry).
     */
    suspend fun revokePremiumAccess()

    /** Non-reactive snapshot of current premium status. */
    fun hasPremiumAccess(): Boolean
}

/**
 * Stub implementation of [PaywallRepository].
 *
 * Currently always returns `false` (no premium).
 * Replace the body of [grantPremiumAccess] with real server-side verification
 * once the eSewa/Khalti WebView payment flow is wired up.
 */
class StubPaywallRepository : PaywallRepository {

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: Flow<Boolean> = _isPremium.asStateFlow()

    override suspend fun grantPremiumAccess() {
        // TODO: Verify payment with Supabase Edge Function before granting access.
        _isPremium.value = true
    }

    override suspend fun revokePremiumAccess() {
        _isPremium.value = false
    }

    override fun hasPremiumAccess(): Boolean = _isPremium.value
}
