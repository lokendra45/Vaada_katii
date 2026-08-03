package com.gaatho.rent.features.payment.presentation.add

import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.persistentListOf
import org.orbitmvi.orbit.viewmodel.orbitContainer

class AddPaymentViewModel : MviViewModel<AddPaymentState, AddPaymentEffect, AddPaymentAction>() {

    override val container = orbitContainer<AddPaymentState, AddPaymentEffect>(AddPaymentState()) {
        loadInitialData()
    }

    private fun loadInitialData() = intent {
        // Mock data loading
        val tenants = persistentListOf(
            TenantSelectionModel("1", "Suman Shrestha"),
            TenantSelectionModel("2", "Alice Smith"),
            TenantSelectionModel("3", "John Doe")
        )
        val properties = persistentListOf(
            PropertySelectionModel("1", "Downtown Lofts - Unit 4B"),
            PropertySelectionModel("2", "Maple Estates - 24 Maple St")
        )
        reduce {
            state.copy(
                tenantsState = UiState.Success(tenants),
                propertiesState = UiState.Success(properties),
                paymentDate = "02-Aug-2026"
            )
        }
    }

    override fun onAction(action: AddPaymentAction) {
        when (action) {
            is AddPaymentAction.OnAmountChanged -> intent {
                // simple numeric validation if needed
                reduce { state.copy(amount = action.amount) }
            }
            is AddPaymentAction.OnTenantSelected -> intent {
                reduce { state.copy(selectedTenantId = action.id) }
            }
            is AddPaymentAction.OnPropertySelected -> intent {
                reduce { state.copy(selectedPropertyId = action.id) }
            }
            is AddPaymentAction.OnPaymentDateChanged -> intent {
                reduce { state.copy(paymentDate = action.date) }
            }
            is AddPaymentAction.OnPaymentMethodSelected -> intent {
                reduce { state.copy(selectedPaymentMethod = action.method) }
            }
            is AddPaymentAction.OnRemarksChanged -> intent {
                reduce { state.copy(remarks = action.remarks) }
            }
            is AddPaymentAction.OnAgreementToggled -> intent {
                reduce { state.copy(isReceiptAgreed = action.agreed) }
            }
            is AddPaymentAction.OnRecordPaymentClicked -> intent {
                if (!state.canSubmit) {
                    postSideEffect(AddPaymentEffect.ShowToast("Please fill all required fields"))
                    return@intent
                }
                reduce { state.copy(isSaving = true) }
                // Simulate save delay
                kotlinx.coroutines.delay(1000)
                reduce { state.copy(isSaving = false, isSuccess = true) }
                postSideEffect(AddPaymentEffect.ShowToast("Payment recorded successfully"))
                postSideEffect(AddPaymentEffect.NavigateBack)
            }
            is AddPaymentAction.OnBackClicked -> intent {
                postSideEffect(AddPaymentEffect.NavigateBack)
            }
        }
    }
}
