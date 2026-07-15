package com.gaatho.rent.features.property.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a Property (e.g., a House or Building).
 *
 * @property id Unique identifier for the property.
 * @property ownerId The ID of the landlord who owns this property.
 * @property name The display name of the house (e.g., "Peaceful Villa").
 * @property address The physical location of the property.
 * @property imageUrl Optional URL for a cover photo of the property.
 * @property propertyType The category of the property (defaults to "HOUSE").
 * @property createdAt Timestamp when the property was first added.
 * @property updatedAt Timestamp of the last modification.
 */
@Serializable
data class Property(
    val id: String,
    val ownerId: String,
    val name: String,
    val address: String,
    val imageUrl: String? = null,
    val propertyType: String = "HOUSE",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
