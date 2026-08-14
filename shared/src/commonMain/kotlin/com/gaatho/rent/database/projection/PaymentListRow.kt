package com.gaatho.rent.database.projection

import androidx.room3.ColumnInfo

/**
 * Narrow projection for the Payment list screen.
 *
 * ONLY contains the columns the list UI actually renders.
 * Does NOT include: encrypted notes, sync_status, last_sync_error,
 * created_at, updated_at — none of which are shown in the list.
 *
 * This prevents the [EncryptedStringConverter] from decrypting
 * the notes column on every list row (expensive crypto operation).
 *
 * Used by: [PaymentDao.selectPagedPaymentListRows]
 */
data class PaymentListRow(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    @ColumnInfo(name = "tenant_id") val tenantId: String?,
    @ColumnInfo(name = "property_id") val propertyId: String?,
    @ColumnInfo(name = "amount") val amount: Long,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "payment_method") val paymentMethod: String?,

    // JOINed fields — come from SQL, no extra queries
    @ColumnInfo(name = "tenant_name") val tenantName: String?,
    @ColumnInfo(name = "tenant_room_number") val tenantRoomNumber: String?,
    @ColumnInfo(name = "property_name") val propertyName: String?
)
