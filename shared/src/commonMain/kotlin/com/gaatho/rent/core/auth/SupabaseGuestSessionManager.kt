package com.gaatho.rent.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.gaatho.rent.core.logging.AppLogger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlin.concurrent.Volatile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val GUEST_MODE_KEY = booleanPreferencesKey("guest_mode_active")

/**
 * DataStore-backed [GuestSessionManager] that signs the user in anonymously with
 * Supabase Auth so guests have a real `auth.uid()` for row-level security.
 */
class SupabaseGuestSessionManager(
    private val supabase: SupabaseClient,
    private val dataStore: DataStore<Preferences>
) : GuestSessionManager {

    @Volatile
    private var cachedGuestMode: Boolean? = null

    override suspend fun ensureGuestSession(): String {
        val existingUser = supabase.auth.currentUserOrNull()
        if (existingUser != null) {
            // A real (email/Google) account is already active — do NOT relabel it as a guest.
            if (existingUser.isAnonymous != true) {
                AppLogger.auth.d { "Real user session active (${existingUser.id}); not creating a guest session" }
                clearGuestSession()
                return existingUser.id
            }
            AppLogger.auth.d { "Guest session already active with uid: ${existingUser.id}" }
            markGuestModeActive()
            return existingUser.id
        }

        AppLogger.auth.d { "Signing in anonymously for guest session" }
        supabase.auth.signInAnonymously()
        val uid = supabase.auth.currentUserOrNull()?.id
            ?: error("Anonymous sign-in completed but no user was returned")
        markGuestModeActive()
        AppLogger.auth.i { "Guest session created with uid: $uid" }
        return uid
    }

    override fun hasActiveGuestSession(): Boolean {
        cachedGuestMode?.let { return it }
        // DataStore reads are already cached; this is a last-resort sync read used only
        // when no Supabase session exists at all. Failure must not crash the caller.
        val active = try {
            runBlocking {
                dataStore.data.first()[GUEST_MODE_KEY] ?: false
            }
        } catch (e: Exception) {
            AppLogger.auth.e(e) { "Failed to read guest-mode flag from DataStore" }
            false
        }
        cachedGuestMode = active
        return active
    }

    override suspend fun clearGuestSession() {
        cachedGuestMode = false
        dataStore.edit { it[GUEST_MODE_KEY] = false }
    }

    private suspend fun markGuestModeActive() {
        cachedGuestMode = true
        dataStore.edit { it[GUEST_MODE_KEY] = true }
    }
}