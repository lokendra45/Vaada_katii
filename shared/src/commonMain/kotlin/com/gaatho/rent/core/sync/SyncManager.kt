package com.gaatho.rent.core.sync

import kotlinx.coroutines.flow.Flow

/**
 * Interface that handles requesting syncs and observing sync state.
 * Modeled after the "Now in Android" sync architecture.
 */
interface SyncManager {
    /**
     * True if a sync operation is currently in progress.
     */
    val isSyncing: Flow<Boolean>

    /**
     * Manually request a sync.
     * This will schedule a background sync operation (e.g. using WorkManager on Android).
     */
    fun requestSync()
}
