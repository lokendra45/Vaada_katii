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
    createdAt = createdAt,
    updatedAt = updatedAt
)
