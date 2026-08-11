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
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PropertyEntity::class,
            parentColumns = ["id"],
            childColumns = ["property_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["owner_id"]),
        Index(value = ["tenant_id"]),
        Index(value = ["property_id"])
    ]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    @ColumnInfo(name = "tenant_id")
    val tenantId: String,
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
    val lastSyncError: String? = null
)

