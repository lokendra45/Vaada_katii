package com.gaatho.rent.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.gaatho.rent.core.database.security.SecretString

@Entity(
    tableName = "tenant",
    foreignKeys = [
        ForeignKey(
            entity = PropertyEntity::class,
            parentColumns = ["id"],
            childColumns = ["property_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["owner_id"]),
        Index(value = ["property_id"])
    ]
)
data class TenantEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    val name: String,
    val email: SecretString?,
    val phone: SecretString?,
    @ColumnInfo(name = "property_id")
    val propertyId: String?,
    @ColumnInfo(name = "room_number")
    val roomNumber: String?,
    @ColumnInfo(name = "rent_amount")
    val rentAmount: Long,
    val status: String = "Active",
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)

