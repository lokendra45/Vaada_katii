package com.gaatho.rent.core.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A simple JSON-based cache using DataStore for offline fallback.
 */
class DataStoreCache(
    private val dataStore: DataStore<Preferences>,
    private val json: Json
) {
    suspend fun <T> put(key: String, value: T, serializer: KSerializer<T>) {
        val prefKey = stringPreferencesKey("cache_$key")
        val jsonString = json.encodeToString(serializer, value)
        dataStore.edit { prefs ->
            prefs[prefKey] = jsonString
        }
    }

    suspend fun <T> get(key: String, serializer: KSerializer<T>): T? {
        val prefKey = stringPreferencesKey("cache_$key")
        val prefs = dataStore.data.first()
        val jsonString = prefs[prefKey] ?: return null
        return try {
            json.decodeFromString(serializer, jsonString)
        } catch (e: Exception) {
            null
        }
    }
}
