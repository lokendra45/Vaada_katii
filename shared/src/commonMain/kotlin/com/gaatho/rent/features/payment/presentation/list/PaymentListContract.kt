package com.gaatho.rent.features.payment.presentation.list

import kotlinx.serialization.Serializable

@Serializable
data class PaymentDisplayModel(
    val id: String,
    val tenantId: String,
    val tenantName: String,
    val propertyName: String,
    /** ISO date (YYYY-MM-DD) for grouping/sorting. */
    val date: String,
    val dateLabel: String,
    val amount: Long,
    val formattedAmount: String,
    val status: String,
    val isPaid: Boolean,
    val unit: String? = null,
    val paymentMethod: String? = null
)

@Serializable
data class PaymentListState(
    val selectedStatus: String = PaymentListFilters.AllStatuses,
    val selectedMonth: String = PaymentListFilters.AllMonths,
    val debouncedQuery: String = ""
)

object PaymentListFilters {
    const val AllStatuses = "All statuses"
    const val Paid = "Paid"
    const val Overdue = "Overdue"
    const val Pending = "Pending"

    const val AllMonths = "All months"
    const val ThisMonth = "This month"
    const val LastMonth = "Last month"
}

sealed interface PaymentListSideEffect {
    data class NavigateToPaymentDetails(val paymentId: String) : PaymentListSideEffect
    data object NavigateBack : PaymentListSideEffect
    data object NavigateToAddPayment : PaymentListSideEffect
}

sealed interface PaymentListAction {
    data object OnBackClicked : PaymentListAction
    data class OnStatusFilterChanged(val status: String) : PaymentListAction
    data class OnMonthFilterChanged(val month: String) : PaymentListAction
    data class OnPaymentClicked(val paymentId: String) : PaymentListAction
    data object OnAddPaymentClicked : PaymentListAction
    data object OnRetry : PaymentListAction
}
