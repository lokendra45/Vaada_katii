package com.gaatho.rent.features.payment.presentation.add

import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.utils.UuidUtil
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.viewmodel.orbitContainer

class AddPaymentViewModel(
    private val tenantIdArg: String?,
    private val propertyIdArg: String?,
    private val sessionManager: SessionManager,
    private val tenantRepository: TenantRepository,
    private val propertyRepository: PropertyRepository,
    private val paymentRepository: PaymentRepository
) : MviViewModel<AddPaymentState, AddPaymentEffect, AddPaymentAction>() {

    override val container = orbitContainer<AddPaymentState, AddPaymentEffect>(AddPaymentState()) {
        loadInitialData()
    }

    private fun loadInitialData() = intent {
        // Set default date to today
        reduce { state.copy(paymentDate = DateTimeUtil.nowIsoString().substring(0, 10)) }

        val ownerId = (sessionManager.currentUserId() ?: "")

        val tenantsFlow = tenantRepository.getTenants(ownerId)
        val propertiesFlow = propertyRepository.getProperties(ownerId)

        combine(tenantsFlow, propertiesFlow) { tenants, properties ->
            val tenantModels = tenants.filter { it.status.equals("Active", ignoreCase = true) }.map { t ->
                TenantSelectionModel(
                    id = t.id,
                    name = t.name,
                    propertyId = t.propertyId,
                    rentAmount = t.rentAmount,
                    roomNumber = t.roomNumber
                )
            }.toImmutableList()

            val propertyModels = properties.map { p ->
                PropertySelectionModel(p.id, p.name, p.totalUnits)
            }.toImmutableList()

            tenantModels to propertyModels
        }.collectLatest { (tenantModels, propertyModels) ->
            val currentPropertyId = state.selectedPropertyId
                ?: propertyIdArg
                ?: if (propertyModels.size == 1) propertyModels.first().id else null

            val filteredTenants = filterTenants(tenantModels, currentPropertyId)
            
            val newSelectedTenantId = state.selectedTenantId
                ?: tenantIdArg
                ?: if (filteredTenants.size == 1) filteredTenants.first().id else null

            val autoTenant = tenantModels.find { it.id == newSelectedTenantId }
            val currentAmountText = state.amount.text
            val newAmountText = if (autoTenant != null && (currentAmountText.isBlank() || currentAmountText == "0" || newSelectedTenantId != state.selectedTenantId)) {
                (autoTenant.rentAmount).toString()
            } else {
                currentAmountText
            }

            reduce {
                state.copy(
                    allTenants = tenantModels,
                    tenantsState = UiState.Success(filteredTenants),
                    propertiesState = UiState.Success(propertyModels),
                    selectedPropertyId = currentPropertyId,
                    selectedTenantId = newSelectedTenantId,
                    selectedUnit = state.selectedUnit ?: autoTenant?.roomNumber,
                    amount = state.amount.copy(text = newAmountText)
                )
            }

            if (tenantModels.isEmpty()) {
                postSideEffect(AddPaymentEffect.ShowSnackbar("Please add at least one active tenant before recording a payment.", isError = true))
                postSideEffect(AddPaymentEffect.NavigateBack)
            }
        }
    }

    override fun onAction(action: AddPaymentAction) {
        when (action) {
            is AddPaymentAction.OnAmountChanged -> intent {
                reduce { state.copy(amount = action.value) }
            }

            is AddPaymentAction.OnPropertySelected -> intent {
                val filtered = filterTenants(state.allTenants, action.id)
                val newTenantId = if (state.allTenants.find { it.id == state.selectedTenantId }?.propertyId == action.id)
                    state.selectedTenantId
                else if (filtered.size == 1)
                    filtered.first().id
                else null

                val autoTenant = state.allTenants.find { it.id == newTenantId }
                val currentAmountText = state.amount.text
                val newAmountText = if (autoTenant != null && (currentAmountText.isBlank() || currentAmountText == "0" || newTenantId != state.selectedTenantId)) {
                    (autoTenant.rentAmount).toString()
                } else {
                    currentAmountText
                }

                reduce {
                    state.copy(
                        selectedPropertyId = action.id,
                        selectedUnit = autoTenant?.roomNumber,
                        selectedTenantId = newTenantId,
                        tenantsState = UiState.Success(filtered),
                        amount = state.amount.copy(text = newAmountText)
                    )
                }
            }

            is AddPaymentAction.OnUnitSelected -> intent {
                reduce { state.copy(selectedUnit = action.unit) }
            }

            is AddPaymentAction.OnTenantSelected -> intent {
                val tenant = state.allTenants.find { it.id == action.id }
                reduce {
                    state.copy(
                        selectedTenantId = action.id,
                        // Auto-select property from tenant's assignment if not already set
                        selectedPropertyId = state.selectedPropertyId ?: tenant?.propertyId,
                        // Auto-select unit from tenant
                        selectedUnit = state.selectedUnit ?: tenant?.roomNumber,
                        // Always fill amount with new tenant's rent
                        amount = state.amount.copy(text = (tenant?.rentAmount ?: 0L).toString())
                    )
                }
            }

            is AddPaymentAction.OnPaymentDateChanged -> intent {
                reduce { state.copy(paymentDate = action.date, showDatePicker = false) }
            }

            is AddPaymentAction.OnDateFieldClicked -> intent {
                reduce { state.copy(showDatePicker = true) }
            }

            is AddPaymentAction.OnDatePickerDismissed -> intent {
                reduce { state.copy(showDatePicker = false) }
            }

            is AddPaymentAction.OnPaymentMethodSelected -> intent {
                reduce { state.copy(selectedPaymentMethod = action.method) }
            }

            is AddPaymentAction.OnRemarksChanged -> intent {
                reduce { state.copy(remarks = action.value) }
            }

            is AddPaymentAction.OnAgreementToggled -> intent {
                reduce { state.copy(isReceiptAgreed = action.agreed) }
            }

            is AddPaymentAction.OnRecordPaymentClicked -> intent {
                if (!state.canSubmit) {
                    postSideEffect(AddPaymentEffect.ShowSnackbar("Please fill all required fields", isError = true))
                    return@intent
                }
                reduce { state.copy(isSaving = true) }

                val amountLong = state.amount.text.toLongOrNull() ?: 0L
                val ownerId = (sessionManager.currentUserId() ?: "")

                val payment = Payment(
                    id = UuidUtil.generateV7String(),
                    ownerId = ownerId,
                    tenantId = state.selectedTenantId!!,
                    propertyId = state.selectedPropertyId!!,
                    amount = amountLong,
                    date = state.paymentDate,
                    status = "Paid",
                    paymentMethod = state.selectedPaymentMethod?.name,
                    roomNumber = state.selectedUnit,
                    notes = state.remarks.text.ifBlank { null },
                    createdAt = DateTimeUtil.nowIsoString(),
                    updatedAt = DateTimeUtil.nowIsoString()
                )

                val result = paymentRepository.createPayment(payment)

                reduce { state.copy(isSaving = false) }

                if (result is com.skydoves.sandwich.ApiResponse.Success) {
                    reduce { state.copy(isSuccess = true) }
                    postSideEffect(AddPaymentEffect.ShowSnackbar("Payment recorded successfully!"))
                    postSideEffect(AddPaymentEffect.NavigateBack)
                } else {
                    postSideEffect(AddPaymentEffect.ShowSnackbar("Failed to record payment. Please try again.", isError = true))
                }
            }

            is AddPaymentAction.OnBackClicked -> intent {
                postSideEffect(AddPaymentEffect.NavigateBack)
            }
        }
    }

    private fun filterTenants(
        all: ImmutableList<TenantSelectionModel>,
        propertyId: String?
    ): ImmutableList<TenantSelectionModel> {
        return if (propertyId == null) all
        else all.filter { it.propertyId == propertyId }.toImmutableList()
    }
}
