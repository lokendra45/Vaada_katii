package com.gaatho.rent.features.payment.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a rent or utility payment made by a tenant.
 *
 * @property id Unique UUID for the payment.
 * @property ownerId The ID of the landlord.
 * @property tenantId The ID of the tenant who made the payment.
 * @property propertyId The ID of the property associated with the payment.
 * @property amount The payment amount in NPR.
 * @property date ISO timestamp or date string of when the payment was made.
 * @property status Current status ("Paid", "Pending", "Overdue").
 * @property paymentMethod How it was paid (e.g. "Cash", "Bank Transfer", "eSewa").
 * @property notes Optional notes or remarks.
 * @property createdAt ISO timestamp when first recorded.
 * @property updatedAt ISO timestamp of last update.
 */
@Serializable
data class Payment(
    val id: String,
    val ownerId: String,
    val tenantId: String,
    val propertyId: String? = null,
    val amount: Long,
    val date: String,
    val status: String = "Paid",
    val paymentMethod: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
