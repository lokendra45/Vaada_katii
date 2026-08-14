package com.gaatho.rent.features.tenant.presentation.details

import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TenantTransactionDisplayModel(
    val id: String,
    val type: String, // e.g. "Rent Payment", "Maintenance Fee"
    val date: String, // e.g. "Nov 1, 2023"
    val amount: String, // e.g. "NPR 25,000"
    val status: String, // e.g. "Paid"
    val isPaid: Boolean,
    val method: String? = null // e.g. "eSewa", "Cash"
)

@Serializable
data class TenantLeaseDisplayModel(
    val monthlyRent: String,
    val status: String, // e.g. "Active"
    val isActive: Boolean,
    val startDate: String, // e.g. "Sept 1, 2023"
    val endDate: String,   // e.g. "Aug 31, 2024"
    val leaseTerm: String = "12 Months", // e.g. "12 Months"
    val isRenewable: Boolean = true,
    val securityDeposit: String? = null, // e.g. "NPR 50,000"
    val paymentDueDate: String? = null   // e.g. "5th of every month"
)

@Serializable
data class TenantProfileDisplayModel(
    val id: String,
    val name: String,
    val address: String, // e.g. "Bakhundole, Lalitpur"
    val isVerified: Boolean,
    val avatarUrl: String? = null, // For placeholder or actual URL
    val phone: String? = null,     // e.g. "+977 98510-23456"
    val movedInDate: String? = null // e.g. "12 July 2023"
)

/**
 * Represents the immutable UI state for the Tenant Details screen.
 */
@Serializable
data class TenantDetailsState(
    val tenantId: String = ""
)

/**
 * Actions triggered by the user on the Tenant Details UI.
 */
sealed interface TenantDetailsAction {
    data object OnBackClicked : TenantDetailsAction
}

/**
 * Side effects triggered by the ViewModel.
 */
sealed interface TenantDetailsEffect {
    data object NavigateBack : TenantDetailsEffect
}
