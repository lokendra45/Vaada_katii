package com.gaatho.rent.core.sync

import kotlinx.serialization.Serializable

@Serializable
data class SyncPayload(
    val properties: List<PropertySyncModel>,
    val tenants: List<TenantSyncModel>,
    val payments: List<PaymentSyncModel>
)

@Serializable
data class PropertySyncModel(
    val id: String,
    val owner_id: String,
    val name: String,
    val address: String,
    val image_url: String?,
    val property_type: String,
    val total_units: Int,
    val billing_cycle: String,
    val amenities: List<String>,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class TenantSyncModel(
    val id: String,
    val owner_id: String,
    val property_id: String?,
    val name: String,
    val email: String?,
    val phone: String?,
    val room_number: String?,
    val rent_amount: Long,
    val status: String,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class PaymentSyncModel(
    val id: String,
    val owner_id: String,
    val tenant_id: String,
    val property_id: String?,
    val amount: Long,
    val date: String,
    val payment_method: String?,
    val status: String,
    val notes: String?,
    val created_at: String,
    val updated_at: String
)
