package com.gaatho.rent.core.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A JSON-based cache using DataStore for offline fallback.
 *
 * Each entry stores the serialized payload alongside a `cachedAt` epoch-ms
 * timestamp so the caller can decide whether the data is still fresh.
 *
 * @param defaultMaxAgeMs How long a cached entry is considered valid (default: 5 minutes).
 *   Pass [Long.MAX_VALUE] for entries that should never expire (e.g. user preferences).
 */
class DataStoreCache(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
    val defaultMaxAgeMs: Long = 5 * 60 * 1000L // 5 minutes
) {
    suspend fun <T> put(key: String, value: T, serializer: KSerializer<T>) {
        val prefKey = stringPreferencesKey("cache_$key")
        val tsKey = longPreferencesKey("cache_ts_$key")
        val jsonString = json.encodeToString(serializer, value)
        dataStore.edit { prefs ->
            prefs[prefKey] = jsonString
            prefs[tsKey] = kotlin.time.Clock.System.now().toEpochMilliseconds()
        }
    }

    /**
     * Returns the cached value if it exists and has not exceeded [maxAgeMs].
     * Returns null if the entry is missing or stale.
     */
    suspend fun <T> get(
        key: String,
        serializer: KSerializer<T>,
        maxAgeMs: Long = defaultMaxAgeMs
    ): T? {
        val prefKey = stringPreferencesKey("cache_$key")
        val tsKey = longPreferencesKey("cache_ts_$key")
        val prefs = dataStore.data.first()
        val jsonString = prefs[prefKey] ?: return null
        val cachedAt = prefs[tsKey] ?: 0L
        val ageMs = kotlin.time.Clock.System.now().toEpochMilliseconds() - cachedAt
        if (ageMs > maxAgeMs) return null  // stale — force fresh network fetch
        return try {
            json.decodeFromString(serializer, jsonString)
        } catch (e: Exception) {
            null
        }
    }
}

