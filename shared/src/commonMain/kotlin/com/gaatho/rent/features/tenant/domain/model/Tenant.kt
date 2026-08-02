package com.gaatho.rent.features.tenant.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a Tenant assigned to a room or property.
 *
 * @property id Unique UUID for the tenant.
 * @property ownerId The ID of the landlord managing this tenant.
 * @property name Full name of the tenant (e.g. "Anita Basnet").
 * @property email Optional contact email.
 * @property phone Optional phone number.
 * @property propertyId ID of the property they are renting.
 * @property propertyName Name of the property (e.g. "Sunrise Residency").
 * @property roomNumber Room or flat identifier (e.g. "Room 4A").
 * @property rentAmount Monthly rent amount in NPR (e.g. 15000).
 * @property status Current occupancy status ("Active", "Inactive", or "Overdue").
 * @property createdAt ISO timestamp when first added.
 * @property updatedAt ISO timestamp of last update.
 */
@Serializable
data class Tenant(
    val id: String,
    val ownerId: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val propertyId: String? = null,
    val propertyName: String? = null,
    val roomNumber: String? = null,
    val rentAmount: Long = 0L,
    val status: String = "Active",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
