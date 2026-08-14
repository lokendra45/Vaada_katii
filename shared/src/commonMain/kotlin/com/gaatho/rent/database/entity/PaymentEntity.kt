package com.gaatho.rent.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.gaatho.rent.core.database.security.SecretString

@Entity(
    tableName = "payment",
    foreignKeys = [
        ForeignKey(
            entity = TenantEntity::class,
            parentColumns = ["id"],
            childColumns = ["tenant_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PropertyEntity::class,
            parentColumns = ["id"],
            childColumns = ["property_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        // payment(owner_id, date, id)
        Index(value = ["owner_id", "date", "id"]),
        // payment(owner_id, status, date, id)
        Index(value = ["owner_id", "status", "date", "id"]),
        // payment(tenant_id, date, id)
        Index(value = ["tenant_id", "date", "id"]),
        // payment(owner_id, property_id, date, id)
        Index(value = ["owner_id", "property_id", "date", "id"]),
        // Idempotency check for production reliability
        Index(value = ["idempotency_key"], unique = true)
    ]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    @ColumnInfo(name = "tenant_id")
    val tenantId: String?,
    @ColumnInfo(name = "property_id")
    val propertyId: String?,
    val amount: Long,
    val date: String,
    val status: String = "Paid",
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String?,
    val notes: SecretString?,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
    @ColumnInfo(name = "sync_status", defaultValue = "PENDING")
    val syncStatus: String = "PENDING",
    @ColumnInfo(name = "last_sync_error")
    val lastSyncError: String? = null,
    @ColumnInfo(name = "device_id", defaultValue = "")
    val deviceId: String = "",
    @ColumnInfo(name = "version", defaultValue = "1")
    val version: Int = 1,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: String? = null,
    @ColumnInfo(name = "idempotency_key")
    val idempotencyKey: String? = null
)

