package com.gaatho.rent.features.tenant.presentation.details

import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TenantTransactionsState(
    val tenantId: String = "",
    @Transient
    val transactionsState: UiState<ImmutableList<TenantTransactionDisplayModel>> = UiState.Idle
)

sealed interface TenantTransactionsAction {
    data object OnPaymentClicked : TenantTransactionsAction
    data object OnViewAllTransactionsClicked : TenantTransactionsAction
    data class OnTransactionClicked(val transactionId: String) : TenantTransactionsAction
}

sealed interface TenantTransactionsEffect {
    data class NavigateToTransactions(val tenantId: String) : TenantTransactionsEffect
    data class ShowToast(val message: String) : TenantTransactionsEffect
}
