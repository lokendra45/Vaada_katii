package com.gaatho.rent.features.payment.presentation.list

import androidx.compose.runtime.Immutable
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PaymentDisplayModel(
    val id: String,
    val tenantName: String,
    val propertyName: String,
    val amountFormatted: String,
    val dateFormatted: String,
    val status: String,
    val paymentMethod: String
)

@Serializable
data class PaymentsListState(
    @Transient
    val paymentsState: UiState<ImmutableList<PaymentDisplayModel>> = UiState.Idle,
    @Transient
    val propertiesState: UiState<ImmutableList<Property>> = UiState.Idle,
    @Transient
    val tenantsState: UiState<ImmutableList<Tenant>> = UiState.Idle,
    val searchQuery: String = "",
    val selectedStatus: String = "All statuses",
    val selectedProperty: String = "All properties",
    @Transient
    val filteredPayments: ImmutableList<PaymentDisplayModel> = persistentListOf()
) {
    val allPayments: ImmutableList<PaymentDisplayModel>
        get() = (paymentsState as? UiState.Success)?.data ?: persistentListOf()

    val totalCount: Int
        get() = allPayments.size
}

sealed interface PaymentsListAction {
    data class OnSearchQueryChanged(val query: String) : PaymentsListAction
    data class OnStatusFilterChanged(val status: String) : PaymentsListAction
    data class OnPropertyFilterChanged(val property: String) : PaymentsListAction
    data class OnPaymentClicked(val paymentId: String) : PaymentsListAction
    data object OnAddPayment : PaymentsListAction
    data object OnRetry : PaymentsListAction
}

sealed interface PaymentsListSideEffect {
    data class NavigateToPaymentDetails(val paymentId: String) : PaymentsListSideEffect
    data object NavigateToAddPayment : PaymentsListSideEffect
}
