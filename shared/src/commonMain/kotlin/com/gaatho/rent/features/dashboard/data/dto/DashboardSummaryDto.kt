package com.gaatho.rent.features.dashboard.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Result of the `get_dashboard_summary` Postgres function — all home screen
 * aggregates computed server-side in a single round-trip.
 */
@Serializable
data class DashboardSummaryDto(
    @SerialName("total_rent") val totalRent: Long = 0L,
    @SerialName("collected_rent") val collectedRent: Long = 0L,
    @SerialName("previous_collected_rent") val previousCollectedRent: Long = 0L,
    @SerialName("outstanding_rent") val outstandingRent: Long = 0L,
    @SerialName("properties_count") val propertiesCount: Long = 0L,
    @SerialName("tenants_count") val tenantsCount: Long = 0L,
    @SerialName("overdue_tenants_count") val overdueTenantsCount: Long = 0L,
    @SerialName("chart_data") val chartData: List<Float> = emptyList(),
    @SerialName("recent_properties") val recentProperties: List<DashboardPropertyDto> = emptyList(),
    @SerialName("recent_payments") val recentPayments: List<RecentPaymentSummaryDto> = emptyList()
)

@Serializable
data class DashboardPropertyDto(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("location") val location: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("totalUnits") val totalUnits: Int = 0,
    @SerialName("occupiedUnits") val occupiedUnits: Int = 0
)

@Serializable
data class RecentPaymentSummaryDto(
    @SerialName("tenant_id") val tenantId: String? = null,
    @SerialName("tenant_name") val tenantName: String? = null,
    @SerialName("unit_number") val unitNumber: String? = null,
    @SerialName("date") val date: String = "",
    @SerialName("amount") val amount: Long = 0L,
    @SerialName("is_paid") val isPaid: Boolean = false
)