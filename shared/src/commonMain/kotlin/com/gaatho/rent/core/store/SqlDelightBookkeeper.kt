package com.gaatho.rent.core.store

import com.gaatho.rent.database.RentManagerDatabase
import org.mobilenativefoundation.store.store5.Bookkeeper

/**
 * SQLDelight-backed implementation of Store5's [Bookkeeper].
 *
 * Tracks the last failed sync timestamp for each key, enabling Store5
 * to retry failed remote writes. All methods must be implemented correctly
 * — silent no-ops will silently break cache invalidation.
 *
 * @param database The local SQLDelight database instance.
 */
class SqlDelightBookkeeper(
    private val database: RentManagerDatabase
) : Bookkeeper<String> {

    override suspend fun getLastFailedSync(key: String): Long? {
        return database.rentManagerQueries.selectBookkeeper(key).executeAsOneOrNull()
    }

    override suspend fun setLastFailedSync(key: String, timestamp: Long): Boolean {
        database.rentManagerQueries.insertBookkeeper(key, timestamp)
        return true
    }

    override suspend fun clear(key: String): Boolean {
        database.rentManagerQueries.deleteBookkeeper(key)
        return true
    }

    override suspend fun clearAll(): Boolean {
        database.rentManagerQueries.deleteAllBookkeeper()
        return true
    }
}
