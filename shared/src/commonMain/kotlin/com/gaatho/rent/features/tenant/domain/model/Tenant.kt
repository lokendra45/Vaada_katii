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
 * @property profileImageUrl URL to the tenant's profile picture.
 * @property documentType Type of document (e.g. "Citizenship", "Passport").
 * @property documentUrl URL to the uploaded document.
 * @property hasWifi Whether the tenant is subscribed to WiFi.
 * @property hasWater Whether the tenant pays for water.
 * @property hasElectricity Whether the tenant pays for electricity.
 * @property hasWaste Whether the tenant pays for waste management.
 * @property leaseDuration The duration of the lease (e.g. "1 Year").
 * @property moveInDate The date the tenant moved in.
 * @property paymentDueDate The recurring date rent is due (e.g. "1st of month").
 * @property securityDeposit The security deposit paid by the tenant.
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
    val profileImageUrl: String? = null,
    val documentType: String? = null,
    val documentUrl: String? = null,
    val hasWifi: Boolean = false,
    val hasWater: Boolean = false,
    val hasElectricity: Boolean = false,
    val hasWaste: Boolean = false,
    val leaseDuration: String? = null,
    val moveInDate: String? = null,
    val paymentDueDate: String? = null,
    val securityDeposit: Long = 0L,
    val status: String = "Active",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
