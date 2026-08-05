package com.gaatho.rent.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.gaatho.rent.database.entity.AppSettingsEntity

@Dao
interface AppSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetting(setting: AppSettingsEntity)

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun selectSetting(key: String): String?

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)
}

