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
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
    @SerialName("document_type") val documentType: String? = null,
    @SerialName("document_url") val documentUrl: String? = null,
    @SerialName("has_wifi") val hasWifi: Boolean = false,
    @SerialName("has_water") val hasWater: Boolean = false,
    @SerialName("has_electricity") val hasElectricity: Boolean = false,
    @SerialName("has_waste") val hasWaste: Boolean = false,
    @SerialName("lease_duration") val leaseDuration: String? = null,
    @SerialName("move_in_date") val moveInDate: String? = null,
    @SerialName("payment_due_date") val paymentDueDate: String? = null,
    @SerialName("security_deposit") val securityDeposit: Long = 0L,
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
    profileImageUrl = profileImageUrl,
    documentType = documentType,
    documentUrl = documentUrl,
    hasWifi = hasWifi,
    hasWater = hasWater,
    hasElectricity = hasElectricity,
    hasWaste = hasWaste,
    leaseDuration = leaseDuration,
    moveInDate = moveInDate,
    paymentDueDate = paymentDueDate,
    securityDeposit = securityDeposit,
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
    profileImageUrl = profileImageUrl,
    documentType = documentType,
    documentUrl = documentUrl,
    hasWifi = hasWifi,
    hasWater = hasWater,
    hasElectricity = hasElectricity,
    hasWaste = hasWaste,
    leaseDuration = leaseDuration,
    moveInDate = moveInDate,
    paymentDueDate = paymentDueDate,
    securityDeposit = securityDeposit,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)
