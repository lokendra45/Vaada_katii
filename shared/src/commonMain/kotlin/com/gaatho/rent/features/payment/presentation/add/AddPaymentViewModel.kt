package com.gaatho.rent.features.payment.presentation.add

import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import org.orbitmvi.orbit.viewmodel.orbitContainer
import com.gaatho.rent.core.utils.UuidUtil

class AddPaymentViewModel(
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

        sessionManager.currentUser.filterNotNull().collectLatest { user ->
            val ownerId = user.id
            
            val tenantsFlow = tenantRepository.getTenants(ownerId)
            val propertiesFlow = propertyRepository.getProperties(ownerId)
            
            combine(tenantsFlow, propertiesFlow) { tenants, properties ->
                val tenantModels = tenants.filter { it.status == "Active" }.map { 
                    TenantSelectionModel(it.id, it.name) 
                }.toImmutableList()
                
                val propertyModels = properties.map { 
                    PropertySelectionModel(it.id, it.name) 
                }.toImmutableList()
                
                tenantModels to propertyModels
            }.collectLatest { (tenantModels, propertyModels) ->
                reduce { 
                    state.copy(
                        tenantsState = UiState.Success(tenantModels),
                        propertiesState = UiState.Success(propertyModels),
                        // Auto-select if only 1 item
                        selectedTenantId = state.selectedTenantId ?: if (tenantModels.size == 1) tenantModels.first().id else null,
                        selectedPropertyId = state.selectedPropertyId ?: if (propertyModels.size == 1) propertyModels.first().id else null
                    )
                }
            }
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
                
                val amountLong = state.amount.toLongOrNull() ?: 0L
                val ownerId = sessionManager.currentUserId() ?: return@intent
                
                val payment = Payment(
                    id = UuidUtil.generateV7String(),
                    ownerId = ownerId,
                    tenantId = state.selectedTenantId!!,
                    propertyId = state.selectedPropertyId!!,
                    amount = amountLong,
                    date = state.paymentDate,
                    status = "Paid",
                    paymentMethod = state.selectedPaymentMethod?.name,
                    notes = state.remarks,
                    createdAt = DateTimeUtil.nowIsoString(),
                    updatedAt = DateTimeUtil.nowIsoString()
                )

                val result = paymentRepository.createPayment(payment)
                
                reduce { state.copy(isSaving = false) }
                
                if (result is com.skydoves.sandwich.ApiResponse.Success) {
                    reduce { state.copy(isSuccess = true) }
                    postSideEffect(AddPaymentEffect.ShowToast("Payment recorded successfully"))
                    postSideEffect(AddPaymentEffect.NavigateBack)
                } else {
                    postSideEffect(AddPaymentEffect.ShowToast("Failed to record payment"))
                }
            }
            is AddPaymentAction.OnBackClicked -> intent {
                postSideEffect(AddPaymentEffect.NavigateBack)
            }
        }
    }
}
