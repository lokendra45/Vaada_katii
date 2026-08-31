package com.gaatho.rent.features.payment.presentation.edit

import androidx.compose.ui.text.input.TextFieldValue
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.payment.presentation.add.PaymentMethod
import kotlinx.serialization.Transient

data class EditPaymentState(
    val loadState: UiState<EditPaymentData> = UiState.Loading,

    @Transient val amount: TextFieldValue = TextFieldValue(),
    val paymentDate: String = "",
    val selectedMethod: PaymentMethod? = null,
    @Transient val receiptNumber: TextFieldValue = TextFieldValue(),
    @Transient val notes: TextFieldValue = TextFieldValue(),

    val isSaving: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showDatePicker: Boolean = false
) {
    val canSubmit: Boolean
        get() = amount.text.isNotBlank() && paymentDate.isNotBlank() && selectedMethod != null && !isSaving
        
    val availableMethods: List<PaymentMethod> get() = listOf(
        PaymentMethod.CASH, PaymentMethod.ESEWA, PaymentMethod.KHALTI, PaymentMethod.BANK_TRANSFER
    )
}

data class EditPaymentData(
    val paymentId: String,
    val tenantName: String,
    val propertyUnit: String
)

sealed interface EditPaymentSideEffect {
    data object NavigateBack : EditPaymentSideEffect
    data object NavigateToPaymentList : EditPaymentSideEffect
    data class ShowSnackbar(val message: String) : EditPaymentSideEffect
}

sealed interface EditPaymentAction {
    data class OnAmountChanged(val value: TextFieldValue) : EditPaymentAction
    data class OnPaymentDateChanged(val date: String) : EditPaymentAction
    data class OnMethodSelected(val method: PaymentMethod) : EditPaymentAction
    data class OnReceiptNumberChanged(val value: TextFieldValue) : EditPaymentAction
    data class OnNotesChanged(val value: TextFieldValue) : EditPaymentAction
    data object OnDateFieldClicked : EditPaymentAction
    data object OnDatePickerDismissed : EditPaymentAction
    data object OnSaveClicked : EditPaymentAction
    data object OnDeleteClicked : EditPaymentAction
    data object OnDeleteDismissed : EditPaymentAction
    data object OnDeleteConfirmed : EditPaymentAction
    data object OnSuccessDialogDismissed : EditPaymentAction
    data object OnBackClicked : EditPaymentAction
}