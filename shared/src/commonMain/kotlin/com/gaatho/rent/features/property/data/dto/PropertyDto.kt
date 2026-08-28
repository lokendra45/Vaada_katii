package com.gaatho.rent.features.property.data.dto

import com.gaatho.rent.features.property.domain.model.Property
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Supabase Property records.
 */
@Serializable
data class PropertyDto(
    @SerialName("id") val id: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("name") val name: String,
    @SerialName("address") val address: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("property_type") val propertyType: String = "HOUSE",
    @SerialName("total_units") val totalUnits: Int = 1,
    @SerialName("billing_cycle") val billingCycle: String = "1st of the month",
    @SerialName("units") val units: List<String> = emptyList(),
    @SerialName("monthly_rent") val monthlyRent: Long = 0L,
    @SerialName("wifi_charge") val wifiCharge: Long = 0L,
    @SerialName("water_charge") val waterCharge: Long = 0L,
    @SerialName("electricity_charge") val electricityCharge: Long = 0L,
    @SerialName("waste_charge") val wasteCharge: Long = 0L,
    @SerialName("description") val description: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Maps [PropertyDto] to the [Property] domain model.
 */
fun PropertyDto.toDomain() = Property(
    id = id.orEmpty(),
    ownerId = ownerId.orEmpty(),
    name = name,
    address = address,
    imageUrl = imageUrl,
    propertyType = propertyType,
    totalUnits = totalUnits,
    billingCycle = billingCycle,
    units = units.mapNotNull { 
        try { kotlinx.serialization.json.Json.decodeFromString<com.gaatho.rent.features.property.domain.model.PropertyUnit>(it) } 
        catch (e: Exception) { null } 
    },
    monthlyRent = monthlyRent,
    wifiCharge = wifiCharge,
    waterCharge = waterCharge,
    electricityCharge = electricityCharge,
    wasteCharge = wasteCharge,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Maps [Property] domain model to [PropertyDto] for remote storage.
 */
fun Property.toDto() = PropertyDto(
    id = id,
    ownerId = ownerId,
    name = name,
    address = address,
    imageUrl = imageUrl,
    propertyType = propertyType,
    totalUnits = totalUnits,
    billingCycle = billingCycle,
    units = units.map { kotlinx.serialization.json.Json.encodeToString(com.gaatho.rent.features.property.domain.model.PropertyUnit.serializer(), it) },
    monthlyRent = monthlyRent,
    wifiCharge = wifiCharge,
    waterCharge = waterCharge,
    electricityCharge = electricityCharge,
    wasteCharge = wasteCharge,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)
