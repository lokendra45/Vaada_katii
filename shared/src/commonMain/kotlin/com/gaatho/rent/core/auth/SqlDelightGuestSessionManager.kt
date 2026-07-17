package com.gaatho.rent.core.auth

import com.gaatho.rent.database.RentManagerDatabase
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.utils.UuidUtil

private const val GUEST_ID_KEY = "guest_session_id"

/**
 * [GuestSessionManager] implementation backed by the local SQLDelight [RentManagerDatabase].
 *
 * Uses the `app_settings` key-value table for persistence. This avoids adding a new
 * dependency (e.g. multiplatform-settings) and keeps all local state in one place.
 *
 * Thread-safe: SQLDelight transactions are serialized on the IO dispatcher by the caller.
 */
class SqlDelightGuestSessionManager(
    private val database: RentManagerDatabase
) : GuestSessionManager {

    private val queries get() = database.rentManagerQueries

    override fun getOrCreateGuestId(): String {
        val existing = queries.selectSetting(GUEST_ID_KEY).executeAsOneOrNull()
        if (existing != null) {
            AppLogger.auth.d { "Returning existing guest ID: $existing" }
            return existing
        }

        val newId = UuidUtil.randomGuestId()
        queries.upsertSetting(key = GUEST_ID_KEY, settingValue = newId)
        AppLogger.auth.i { "Created new guest ID: $newId" }
        return newId
    }

    override fun clearGuestSession() {
        queries.deleteSetting(GUEST_ID_KEY)
        AppLogger.auth.i { "Guest session cleared." }
    }

    override fun hasActiveGuestSession(): Boolean {
        return queries.selectSetting(GUEST_ID_KEY).executeAsOneOrNull() != null
    }
}
