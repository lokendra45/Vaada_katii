package com.gaatho.rent.database.projection

import androidx.room3.ColumnInfo

/**
 * Narrow projection for the Tenant list screen.
 *
 * ONLY contains the columns the list UI actually renders.
 * Does NOT include: encrypted email, encrypted phone, sync_status,
 * last_sync_error, created_at, updated_at — none of which are shown
 * in the list.
 *
 * This prevents the [EncryptedStringConverter] from decrypting
 * email and phone columns on every list row.
 *
 * propertyName is JOINed from the property table — no extra queries.
 *
 * Used by: [TenantDao.selectPagedTenantListRows]
 */
data class TenantListRow(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    @ColumnInfo(name = "property_id") val propertyId: String?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "room_number") val roomNumber: String?,
    @ColumnInfo(name = "rent_amount") val rentAmount: Long,

    // JOINed field — comes from SQL, no extra query
    @ColumnInfo(name = "property_name") val propertyName: String?
)
