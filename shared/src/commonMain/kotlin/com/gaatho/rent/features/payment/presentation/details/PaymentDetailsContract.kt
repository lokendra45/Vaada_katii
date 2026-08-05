package com.gaatho.rent.features.payment.presentation.details

import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.tenant.domain.model.Tenant

data class PaymentDetailsState(
    val paymentState: UiState<PaymentDetailsData> = UiState.Loading,
    val showShareDialog: Boolean = false
)

data class PaymentDetailsData(
    val payment: Payment,
    val tenant: Tenant?,
    val property: Property?
)

sealed interface PaymentDetailsSideEffect {
    data object NavigateBack : PaymentDetailsSideEffect
    data class ShowError(val message: String) : PaymentDetailsSideEffect
    data class ShowMessage(val message: String) : PaymentDetailsSideEffect
}

sealed interface PaymentDetailsAction {
    data object OnBackClicked : PaymentDetailsAction
    data object OnDownloadReceipt : PaymentDetailsAction
    data object OnShareDetails : PaymentDetailsAction
    data object OnRetry : PaymentDetailsAction
}
