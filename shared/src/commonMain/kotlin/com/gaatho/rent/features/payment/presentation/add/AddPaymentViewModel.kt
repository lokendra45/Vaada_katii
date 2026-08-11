package com.gaatho.rent.features.payment.presentation.add

import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import org.orbitmvi.orbit.viewmodel.orbitContainer
import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.utils.UuidUtil

class AddPaymentViewModel(
    private val sessionManager: SessionManager,
    private val userIdentityProvider: UserIdentityProvider,
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

        val ownerId = userIdentityProvider.currentUserId()

        val tenantsFlow = tenantRepository.getTenants(ownerId)
        val propertiesFlow = propertyRepository.getProperties(ownerId)

        combine(tenantsFlow, propertiesFlow) { tenants, properties ->
            val tenantModels = tenants.filter { it.status == "Active" }.map { t ->
                TenantSelectionModel(
                    id = t.id,
                    name = t.name,
                    propertyId = t.propertyId,
                    rentAmount = t.rentAmount,
                    roomNumber = t.roomNumber
                )
            }.toImmutableList()

            val propertyModels = properties.map { p ->
                PropertySelectionModel(p.id, p.name)
            }.toImmutableList()

            tenantModels to propertyModels
        }.collectLatest { (tenantModels, propertyModels) ->
            reduce {
                val currentPropertyId = state.selectedPropertyId
                    ?: if (propertyModels.size == 1) propertyModels.first().id else null
                
                val filteredTenants = filterTenants(tenantModels, currentPropertyId)
                
                state.copy(
                    allTenants = tenantModels,
                    tenantsState = UiState.Success(filteredTenants),
                    propertiesState = UiState.Success(propertyModels),
                    selectedPropertyId = currentPropertyId,
                    selectedTenantId = state.selectedTenantId
                        ?: if (filteredTenants.size == 1) filteredTenants.first().id else null
                )
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
                reduce {
                    state.copy(
                        selectedPropertyId = action.id,
                        // Reset tenant if their property doesn't match new selection
                        selectedTenantId = if (state.allTenants.find { it.id == state.selectedTenantId }?.propertyId == action.id)
                            state.selectedTenantId else null,
                        tenantsState = UiState.Success(filtered)
                    )
                }
            }

            is AddPaymentAction.OnTenantSelected -> intent {
                val tenant = state.allTenants.find { it.id == action.id }
                reduce {
                    state.copy(
                        selectedTenantId = action.id,
                        // Auto-select property from tenant's assignment if not already set
                        selectedPropertyId = state.selectedPropertyId ?: tenant?.propertyId,
                        // Auto-fill amount only if user hasn't typed one yet
                        amount = if (state.amount.text.isBlank() || state.amount.text == "0")
                            state.amount.copy(text = (tenant?.rentAmount ?: 0L).toString())
                        else state.amount
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
                val ownerId = userIdentityProvider.currentUserId()

                val payment = Payment(
                    id = UuidUtil.generateV7String(),
                    ownerId = ownerId,
                    tenantId = state.selectedTenantId!!,
                    propertyId = state.selectedPropertyId!!,
                    amount = amountLong,
                    date = state.paymentDate,
                    status = "Paid",
                    paymentMethod = state.selectedPaymentMethod?.name,
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
