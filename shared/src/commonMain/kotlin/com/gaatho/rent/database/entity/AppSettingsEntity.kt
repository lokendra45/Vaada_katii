package com.gaatho.rent.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

