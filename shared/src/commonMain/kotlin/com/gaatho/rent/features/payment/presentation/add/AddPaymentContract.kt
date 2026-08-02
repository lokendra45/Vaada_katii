package com.gaatho.rent.features.payment.presentation.add

import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.StringResource
import rentmanagerapp.shared.generated.resources.Res

data class AddPaymentState(
    val amount: String = "",
    val tenantsState: UiState<ImmutableList<TenantSelectionModel>> = UiState.Loading,
    val selectedTenantId: String? = null,
    
    val propertiesState: UiState<ImmutableList<PropertySelectionModel>> = UiState.Loading,
    val selectedPropertyId: String? = null,
    
    val paymentDate: String = "",
    val selectedPaymentMethod: PaymentMethod? = null,
    val remarks: String = "",
    
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false
) {
    val canSubmit: Boolean
        get() = amount.isNotBlank() && amount != "0" &&
                selectedTenantId != null &&
                selectedPropertyId != null &&
                paymentDate.isNotBlank() &&
                selectedPaymentMethod != null &&
                !isSaving
}

data class TenantSelectionModel(
    val id: String,
    val name: String
)

data class PropertySelectionModel(
    val id: String,
    val name: String
)


enum class PaymentMethod(val labelRes: StringResource) {
    CASH(Res.string.payment_method_cash),
    BANK_TRANSFER(Res.string.payment_method_bank_transfer),
    ESEWA(Res.string.payment_method_esewa),
    KHALTI(Res.string.payment_method_khalti)
}

sealed class AddPaymentAction {
    data class OnAmountChanged(val amount: String) : AddPaymentAction()
    data class OnTenantSelected(val id: String) : AddPaymentAction()
    data class OnPropertySelected(val id: String) : AddPaymentAction()
    data class OnPaymentDateChanged(val date: String) : AddPaymentAction()
    data class OnPaymentMethodSelected(val method: PaymentMethod) : AddPaymentAction()
    data class OnRemarksChanged(val remarks: String) : AddPaymentAction()
    
    data object OnRecordPaymentClicked : AddPaymentAction()
    data object OnBackClicked : AddPaymentAction()
}

sealed class AddPaymentEffect {
    data object NavigateBack : AddPaymentEffect()
    data class ShowToast(val message: String) : AddPaymentEffect()
}
