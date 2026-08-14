package com.gaatho.rent.core.auth

import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.utils.UuidUtil
import com.gaatho.rent.database.dao.AppSettingsDao
import com.gaatho.rent.database.entity.AppSettingsEntity
import kotlin.concurrent.Volatile
import kotlin.jvm.Synchronized
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

private const val GUEST_ID_KEY = "guest_session_id"

class LocalGuestSessionManager(
    private val appSettingsDao: AppSettingsDao
) : GuestSessionManager {

    @Volatile
    private var cachedGuestId: String? = null

    override fun getOrCreateGuestId(): String {
        cachedGuestId?.let { return it }
        return getOrCreateGuestIdSynchronized()
    }

    @Suppress("DEPRECATION")
    @Synchronized
    private fun getOrCreateGuestIdSynchronized(): String {
        cachedGuestId?.let { return it }

        // Since this is called synchronously and might be on any thread, runBlocking is needed here
        // or we should reconsider the GuestSessionManager interface. For now, since Room DAOs are suspend for 
        // single queries, we'll use runBlocking on Dispatchers.IO.
        val existing = runBlocking(Dispatchers.IO) { appSettingsDao.selectSetting(GUEST_ID_KEY) }
        
        if (existing != null) {
            AppLogger.auth.d { "Returning existing guest ID: $existing" }
            cachedGuestId = existing
            return existing
        }

        val newGuestId = UuidUtil.generateV7().toString()
        AppLogger.auth.d { "Created new guest ID: $newGuestId" }

        runBlocking(Dispatchers.IO) {
            appSettingsDao.upsertSetting(AppSettingsEntity(GUEST_ID_KEY, newGuestId))
        }

        cachedGuestId = newGuestId
        return newGuestId
    }

    override fun hasActiveGuestSession(): Boolean {
        return cachedGuestId != null || runBlocking(Dispatchers.IO) { appSettingsDao.selectSetting(GUEST_ID_KEY) != null }
    }

    override fun clearGuestSession() {
        cachedGuestId = null
        runBlocking(Dispatchers.IO) {
            appSettingsDao.deleteSetting(GUEST_ID_KEY)
        }
    }
}
