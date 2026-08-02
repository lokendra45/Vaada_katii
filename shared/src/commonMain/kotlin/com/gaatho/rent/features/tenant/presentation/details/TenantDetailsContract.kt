package com.gaatho.rent.features.tenant.presentation.details

import androidx.compose.runtime.Immutable
import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

@Serializable
data class TenantTransactionDisplayModel(
    val id: String,
    val type: String, // e.g. "Rent Payment", "Maintenance Fee"
    val date: String, // e.g. "Nov 1, 2023"
    val amount: String, // e.g. "Rs 45,000"
    val status: String, // e.g. "Paid"
    val isPaid: Boolean
)

@Serializable
data class TenantLeaseDisplayModel(
    val monthlyRent: String,
    val status: String, // e.g. "Active"
    val isActive: Boolean,
    val startDate: String, // e.g. "Sept 1, 2023"
    val endDate: String,   // e.g. "Aug 31, 2024"
    val leaseTerm: String = "12 Months", // e.g. "12 Months"
    val isRenewable: Boolean = true
)

@Serializable
data class TenantProfileDisplayModel(
    val id: String,
    val name: String,
    val address: String, // e.g. "Bakhundole, Lalitpur"
    val isVerified: Boolean,
    val avatarUrl: String? = null // For placeholder or actual URL
)

/**
 * Represents the immutable UI state for the Tenant Details screen.
 */
@Serializable
@Immutable
data class TenantDetailsState(
    val tenantId: String = "",
    val profileState: UiState<TenantProfileDisplayModel> = UiState.Idle,
    val leaseState: UiState<TenantLeaseDisplayModel> = UiState.Idle,
    val transactionsState: UiState<ImmutableList<TenantTransactionDisplayModel>> = UiState.Idle
)

/**
 * Actions triggered by the user on the Tenant Details UI.
 */
sealed interface TenantDetailsAction {
    data object OnBackClicked : TenantDetailsAction
    data object OnPaymentClicked : TenantDetailsAction
    data object OnEmailClicked : TenantDetailsAction
    data object OnCallClicked : TenantDetailsAction
    data object OnMessageClicked : TenantDetailsAction
    data object OnMaintenanceClicked : TenantDetailsAction
    data object OnViewAllTransactionsClicked : TenantDetailsAction
    data class OnTransactionClicked(val transactionId: String) : TenantDetailsAction
}

/**
 * Side effects triggered by the ViewModel.
 */
sealed interface TenantDetailsEffect {
    data object NavigateBack : TenantDetailsEffect
    data class OpenEmailApp(val email: String) : TenantDetailsEffect
    data class OpenPhoneApp(val phone: String) : TenantDetailsEffect
    data class NavigateToTransactions(val tenantId: String) : TenantDetailsEffect
    data class ShowToast(val message: String) : TenantDetailsEffect
}
