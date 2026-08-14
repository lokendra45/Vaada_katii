package com.gaatho.rent.database.projection

import androidx.room3.ColumnInfo
import com.gaatho.rent.core.database.security.SecretString

/**
 * Narrow projection for the Property list screen.
 *
 * ONLY contains the columns the list UI actually renders.
 * Does NOT include: amenities (Set<String> conversion), billing_cycle,
 * monthly_rent (for display the VM uses totalUnits anyway), description,
 * image_url, sync_status, last_sync_error, created_at, updated_at.
 *
 * occupiedUnits and pendingAmount are computed by SQL COUNT/SUM aggregation —
 * NOT loaded by fetching the entire tenant table.
 *
 * Used by: [PropertyDao.selectPagedPropertyListRows]
 */
data class PropertyListRow(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "address") val address: SecretString?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "property_type") val propertyType: String,
    @ColumnInfo(name = "total_units") val totalUnits: Int,
    @ColumnInfo(name = "monthly_rent") val monthlyRent: Long,

    // SQL-aggregated — no tenant table scan in Kotlin
    @ColumnInfo(name = "occupied_units") val occupiedUnits: Int,
    @ColumnInfo(name = "pending_amount") val pendingAmount: Long
)
