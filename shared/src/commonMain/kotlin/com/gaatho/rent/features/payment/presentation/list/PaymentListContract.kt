package com.gaatho.rent.features.payment.presentation.list

import androidx.compose.runtime.Immutable
import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Immutable
data class PaymentDisplayModel(
    val id: String,
    val tenantId: String,
    val tenantName: String,
    val propertyName: String,
    val dateLabel: String,
    val amount: Long,
    val status: String,
    val isPaid: Boolean
)

@Serializable
data class PaymentListState(
    @Transient
    val paymentsState: UiState<ImmutableList<PaymentDisplayModel>> = UiState.Idle,
    val searchQuery: String = "",
    val selectedStatus: String = "All statuses",
    val selectedMonth: String = "All months"
)

sealed interface PaymentListSideEffect {
    data class NavigateToPaymentDetails(val paymentId: String) : PaymentListSideEffect
    data object NavigateBack : PaymentListSideEffect
    data object NavigateToAddPayment : PaymentListSideEffect
}

sealed interface PaymentListAction {
    data object OnBackClicked : PaymentListAction
    data class OnSearchQueryChanged(val query: String) : PaymentListAction
    data class OnStatusFilterChanged(val status: String) : PaymentListAction
    data class OnMonthFilterChanged(val month: String) : PaymentListAction
    data class OnPaymentClicked(val paymentId: String) : PaymentListAction
    data object OnAddPaymentClicked : PaymentListAction
    data object OnRetry : PaymentListAction
}
