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
 * @property units List of unit names (e.g. "Room 101", "Flat A"). Replaces totalUnits integer.
 * @property totalUnits The total number of rentable units (e.g., rooms or flats) in this property.
 * @property monthlyRent The monthly rent for the property in NPR.
 * @property wifiCharge Base wifi charge in NPR.
 * @property waterCharge Base water charge in NPR.
 * @property electricityCharge Base electricity charge in NPR.
 * @property wasteCharge Base waste management charge in NPR.
 * @property description A short description of the property (parking, water, power back-up, etc.).
 * @property billingCycle The standard billing cycle (e.g., "1st of the month").
 * @property amenities A set of amenities provided by the property.
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
    val units: List<PropertyUnit> = emptyList(),
    val totalUnits: Int = 1,
    val monthlyRent: Long = 0L,
    val wifiCharge: Long = 0L,
    val waterCharge: Long = 0L,
    val electricityCharge: Long = 0L,
    val wasteCharge: Long = 0L,
    val description: String = "",
    val billingCycle: String = "1st of the month",
    val amenities: Set<String> = emptySet(),
    val occupiedUnits: Int = 0,
    val pendingAmount: Long = 0L,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
