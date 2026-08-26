package com.gaatho.rent.features.payment.presentation.edit

import androidx.compose.ui.text.input.TextFieldValue
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.gaatho.rent.features.payment.presentation.add.PaymentMethod
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.viewmodel.orbitContainer

class EditPaymentViewModel(
    private val paymentId: String,
    private val paymentRepository: PaymentRepository,
    private val tenantRepository: TenantRepository,
    private val propertyRepository: PropertyRepository,
    private val sessionManager: SessionManager
) : MviViewModel<EditPaymentState, EditPaymentSideEffect, EditPaymentAction>() {

    override val container = orbitContainer<EditPaymentState, EditPaymentSideEffect>(EditPaymentState()) {
        loadPayment()
    }

    private fun loadPayment() = intent {
        reduce { state.copy(loadState = UiState.Loading) }

        val ownerId = (sessionManager.currentUserId() ?: "")
        val payment = paymentRepository.getPaymentById(paymentId).firstOrNull()

        if (payment == null) {
            reduce { state.copy(loadState = UiState.Error("Payment not found")) }
            return@intent
        }

        val tenants = tenantRepository.getTenants(ownerId).firstOrNull() ?: emptyList()
        val properties = propertyRepository.getProperties(ownerId).firstOrNull() ?: emptyList()

        val tenant = tenants.find { it.id == payment.tenantId }
        val property = properties.find { it.id == payment.propertyId }

        val propertyUnit = listOfNotNull(
            property?.name,
            tenant?.roomNumber?.let { "Unit $it" }
        ).joinToString(" - ")

        reduce {
            state.copy(
                loadState = UiState.Success(
                    EditPaymentData(
                        paymentId = payment.id,
                        tenantName = tenant?.name ?: "Unknown Tenant",
                        propertyUnit = propertyUnit.ifBlank { "—" }
                    )
                ),
                amount = TextFieldValue(payment.amount.toString()),
                paymentDate = payment.date.take(10),
                selectedMethod = parsePaymentMethod(payment.paymentMethod),
                notes = TextFieldValue(payment.notes ?: "")
            )
        }
    }

    override fun onAction(action: EditPaymentAction) {
        when (action) {
            is EditPaymentAction.OnAmountChanged -> intent {
                val digits = action.value.text.filter { it.isDigit() }
                reduce { state.copy(amount = action.value.copy(text = digits)) }
            }
            is EditPaymentAction.OnPaymentDateChanged -> intent {
                reduce { state.copy(paymentDate = action.date, showDatePicker = false) }
            }
            is EditPaymentAction.OnMethodSelected -> intent {
                reduce { state.copy(selectedMethod = action.method) }
            }
            is EditPaymentAction.OnReceiptNumberChanged -> intent {
                reduce { state.copy(receiptNumber = action.value) }
            }
            is EditPaymentAction.OnNotesChanged -> intent {
                reduce { state.copy(notes = action.value) }
            }
            is EditPaymentAction.OnDateFieldClicked -> intent {
                reduce { state.copy(showDatePicker = true) }
            }
            is EditPaymentAction.OnDatePickerDismissed -> intent {
                reduce { state.copy(showDatePicker = false) }
            }
            is EditPaymentAction.OnSaveClicked -> updatePayment()
            is EditPaymentAction.OnDeleteClicked -> intent {
                reduce { state.copy(showDeleteConfirm = true) }
            }
            is EditPaymentAction.OnDeleteDismissed -> intent {
                reduce { state.copy(showDeleteConfirm = false) }
            }
            is EditPaymentAction.OnDeleteConfirmed -> deletePayment()
            is EditPaymentAction.OnSuccessDialogDismissed -> intent {
                reduce { state.copy(showSuccessDialog = false) }
                postSideEffect(EditPaymentSideEffect.NavigateBack)
            }
            is EditPaymentAction.OnBackClicked -> intent {
                postSideEffect(EditPaymentSideEffect.NavigateBack)
            }
        }
    }

    private fun updatePayment() = intent {
        val s = state
        if (!s.canSubmit) return@intent

        reduce { state.copy(isSaving = true) }

        val current = paymentRepository.getPaymentById(paymentId).firstOrNull()
        if (current == null) {
            reduce { state.copy(isSaving = false) }
            postSideEffect(EditPaymentSideEffect.ShowSnackbar("Payment not found"))
            return@intent
        }

        val updated = current.copy(
            amount = s.amount.text.toLongOrNull() ?: current.amount,
            date = s.paymentDate,
            paymentMethod = s.selectedMethod?.storage,
            notes = s.notes.text.trim().takeIf { it.isNotBlank() },
            updatedAt = com.gaatho.rent.core.utils.DateTimeUtil.nowIsoString()
        )

        when (val result = paymentRepository.updatePayment(updated)) {
            is ApiResponse.Success -> {
                reduce { state.copy(isSaving = false, showSuccessDialog = true) }
            }
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditPaymentSideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(result, "Failed to update payment")
                ))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditPaymentSideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(result, "Failed to update payment")
                ))
            }
        }
    }

    private fun deletePayment() = intent {
        reduce { state.copy(showDeleteConfirm = false, isSaving = true) }
        when (val result = paymentRepository.deletePayment(paymentId)) {
            is ApiResponse.Success -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditPaymentSideEffect.NavigateBack)
            }
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditPaymentSideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(result, "Failed to delete payment")
                ))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditPaymentSideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(result, "Failed to delete payment")
                ))
            }
        }
    }
}

private fun parsePaymentMethod(value: String?): PaymentMethod? = when (value?.lowercase()) {
    "cash" -> PaymentMethod.CASH
    "bank", "bank_transfer" -> PaymentMethod.BANK_TRANSFER
    "esewa" -> PaymentMethod.ESEWA
    "khalti" -> PaymentMethod.KHALTI
    else -> null
}

private val PaymentMethod.storage: String
    get() = when (this) {
        PaymentMethod.CASH -> "Cash"
        PaymentMethod.BANK_TRANSFER -> "Bank Transfer"
        PaymentMethod.ESEWA -> "eSewa"
        PaymentMethod.KHALTI -> "Khalti"
    }
