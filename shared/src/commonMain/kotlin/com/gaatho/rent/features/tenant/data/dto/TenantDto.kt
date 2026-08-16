package com.gaatho.rent.features.tenant.data.dto

import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Supabase Tenant records.
 *
 * [property] captures the embedded resource from `select(..., property(name))`
 * joins so the tenant list can show the property name without an extra query.
 */
@Serializable
data class TenantDto(
    @SerialName("id") val id: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("property_id") val propertyId: String? = null,
    @SerialName("property") val property: PropertyNameDto? = null,
    @SerialName("room_number") val roomNumber: String? = null,
    @SerialName("rent_amount") val rentAmount: Long = 0L,
    @SerialName("status") val status: String = "Active",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/** Embedded `property` row returned by a `property(name)` join. */
@Serializable
data class PropertyNameDto(
    @SerialName("name") val name: String? = null
)

/**
 * Maps [TenantDto] to the [Tenant] domain model.
 */
fun TenantDto.toDomain() = Tenant(
    id = id.orEmpty(),
    ownerId = ownerId.orEmpty(),
    name = name,
    email = email,
    phone = phone,
    propertyId = propertyId,
    propertyName = property?.name,
    roomNumber = roomNumber,
    rentAmount = rentAmount,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Maps [Tenant] domain model to [TenantDto] for remote cloud storage.
 */
fun Tenant.toDto() = TenantDto(
    id = id,
    ownerId = ownerId,
    name = name,
    email = email,
    phone = phone,
    propertyId = propertyId,
    roomNumber = roomNumber,
    rentAmount = rentAmount,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)
