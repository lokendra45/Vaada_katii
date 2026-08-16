package com.gaatho.rent.features.payment.data.dto

import com.gaatho.rent.features.payment.domain.model.Payment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Supabase payments records.
 *
 * [tenant] and [property] capture the embedded resources returned by
 * `select(..., tenant(name, room_number), property(name))` joins so the payment
 * list can show tenant/property names without extra queries.
 */
@Serializable
data class PaymentDto(
    @SerialName("id") val id: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("tenant_id") val tenantId: String? = null,
    @SerialName("property_id") val propertyId: String? = null,
    @SerialName("amount") val amount: Long = 0L,
    @SerialName("date") val date: String = "",
    @SerialName("status") val status: String = "Paid",
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("idempotency_key") val idempotencyKey: String? = null,
    @SerialName("tenant") val tenant: PaymentTenantDto? = null,
    @SerialName("property") val property: PaymentPropertyDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/** Embedded `tenant` row returned by a `tenant(name, room_number)` join. */
@Serializable
data class PaymentTenantDto(
    @SerialName("name") val name: String? = null,
    @SerialName("room_number") val roomNumber: String? = null
)

/** Embedded `property` row returned by a `property(name)` join. */
@Serializable
data class PaymentPropertyDto(
    @SerialName("name") val name: String? = null
)

/** Maps [PaymentDto] to the [Payment] domain model. */
fun PaymentDto.toDomain() = Payment(
    id = id.orEmpty(),
    ownerId = ownerId.orEmpty(),
    tenantId = tenantId.orEmpty(),
    propertyId = propertyId,
    amount = amount,
    date = date,
    status = status,
    paymentMethod = paymentMethod,
    notes = notes,
    tenantName = tenant?.name,
    propertyName = property?.name,
    roomNumber = tenant?.roomNumber,
    idempotencyKey = idempotencyKey,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/** Maps [Payment] domain model to [PaymentDto] for remote storage. */
fun Payment.toDto() = PaymentDto(
    id = id,
    ownerId = ownerId,
    tenantId = tenantId,
    propertyId = propertyId,
    amount = amount,
    date = date,
    status = status,
    paymentMethod = paymentMethod,
    notes = notes,
    idempotencyKey = idempotencyKey,
    createdAt = createdAt,
    updatedAt = updatedAt
)