package com.gaatho.rent.core.auth

import com.gaatho.rent.database.RentManagerDatabase
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.utils.UuidUtil
import kotlin.concurrent.Volatile
import kotlin.jvm.Synchronized


private const val GUEST_ID_KEY = "guest_session_id"

/**
 * [GuestSessionManager] implementation backed by the local SQLDelight [RentManagerDatabase].
 *
 * Uses the `app_settings` key-value table for persistence. This avoids adding a new
 * dependency (e.g. multiplatform-settings) and keeps all local state in one place.
 *
 * ## Threading
 * The guest ID is loaded from the DB exactly once and then held in [cachedGuestId].
 * All subsequent calls to [getOrCreateGuestId] are pure in-memory lookups — no DB
 * access, no threading concerns, safe to call from any thread or dispatcher.
 *
 * The first call MUST happen on the IO dispatcher. The local repositories own this
 * responsibility via SQLDelight's [mapToList(Dispatchers.IO)] and [withContext(Dispatchers.IO)]
 * on their write operations — the ViewModel does NOT need to specify a dispatcher.
 */
class SqlDelightGuestSessionManager(
    private val database: RentManagerDatabase
) : GuestSessionManager {

    private val queries get() = database.rentManagerQueries

    /**
     * In-memory cache. Volatile for safe publication across threads.
     * Once set, never changes for the lifetime of the process — guest IDs are stable.
     */
    @Volatile
    private var cachedGuestId: String? = null

    override fun getOrCreateGuestId(): String {
        // Fast-path: return cached value without any DB access.
        // @Volatile guarantees cross-thread visibility — no lock needed here.
        cachedGuestId?.let { return it }

        // Slow-path: synchronized so only ONE thread ever does the DB read,
        // even if observeTenants() and observeProperties() fire concurrently.
        return getOrCreateGuestIdSynchronized()
    }

    @Synchronized
    private fun getOrCreateGuestIdSynchronized(): String {
        // Double-checked: another thread may have populated the cache while
        // we were waiting to acquire the lock.
        cachedGuestId?.let { return it }

        val existing = queries.selectSetting(GUEST_ID_KEY).executeAsOneOrNull()
        if (existing != null) {
            AppLogger.auth.d { "Returning existing guest ID: $existing" }
            cachedGuestId = existing
            return existing
        }

        val newId = UuidUtil.randomGuestId()
        queries.upsertSetting(key = GUEST_ID_KEY, settingValue = newId)
        AppLogger.auth.i { "Created new guest ID: $newId" }
        cachedGuestId = newId
        return newId
    }

    override fun clearGuestSession() {
        queries.deleteSetting(GUEST_ID_KEY)
        cachedGuestId = null
        AppLogger.auth.i { "Guest session cleared." }
    }

    override fun hasActiveGuestSession(): Boolean {
        return cachedGuestId != null ||
            queries.selectSetting(GUEST_ID_KEY).executeAsOneOrNull() != null
    }
}
