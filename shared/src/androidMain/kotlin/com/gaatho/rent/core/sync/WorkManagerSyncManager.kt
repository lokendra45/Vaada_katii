package com.gaatho.rent.core.sync

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Android implementation of [SyncManager] using [WorkManager].
 * Modeled after the "Now in Android" sync architecture.
 */
class WorkManagerSyncManager(
    private val context: Context
) : SyncManager {

    private val workManager = WorkManager.getInstance(context)

    override val isSyncing: Flow<Boolean> = workManager.getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
        .map { workInfos ->
            workInfos.any { it.state == WorkInfo.State.RUNNING }
        }

    override fun requestSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
            
        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        const val SYNC_WORK_NAME = "SyncWorkName"
    }
}
