package com.gaatho.rent.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.gaatho.rent.core.database.security.SecretString

@Entity(
    tableName = "property",
    indices = [Index(value = ["owner_id"])]
)
data class PropertyEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    val name: String,
    val address: SecretString,
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,
    @ColumnInfo(name = "property_type")
    val propertyType: String = "HOUSE",
    @ColumnInfo(name = "total_units", defaultValue = "1")
    val totalUnits: Int = 1,
    @ColumnInfo(name = "monthly_rent", defaultValue = "0")
    val monthlyRent: Long = 0L,
    @ColumnInfo(name = "description", defaultValue = "")
    val description: String = "",
    @ColumnInfo(name = "billing_cycle", defaultValue = "1st of the month")
    val billingCycle: String = "1st of the month",
    @ColumnInfo(name = "amenities", defaultValue = "")
    val amenities: Set<String> = emptySet(),
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
    @ColumnInfo(name = "sync_status", defaultValue = "PENDING")
    val syncStatus: String = "PENDING",
    @ColumnInfo(name = "last_sync_error")
    val lastSyncError: String? = null
)

