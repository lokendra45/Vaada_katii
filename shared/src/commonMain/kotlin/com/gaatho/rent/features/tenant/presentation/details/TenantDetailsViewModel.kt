package com.gaatho.rent.features.tenant.presentation.details

import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import kotlinx.coroutines.delay
import kotlinx.collections.immutable.persistentListOf
import org.orbitmvi.orbit.viewmodel.orbitContainer

import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.skydoves.sandwich.ApiResponse
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.collections.immutable.toImmutableList
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.utils.CurrencyUtil

class TenantDetailsViewModel(
    private val tenantId: String,
    private val tenantRepository: TenantRepository,
    private val propertyRepository: PropertyRepository,
    private val paymentRepository: PaymentRepository
) : MviViewModel<TenantDetailsState, TenantDetailsEffect, TenantDetailsAction>() {

    override val container = orbitContainer<TenantDetailsState, TenantDetailsEffect>(
        initialState = TenantDetailsState(tenantId = tenantId)
    ) {
        loadData()
    }

    private fun loadData() = intent(registerIdling = false) {
        val tenantFlow = tenantRepository.getTenantById(tenantId)
        val paymentsFlow = paymentRepository.getPaymentsByTenant(tenantId)

        kotlinx.coroutines.flow.combine(tenantFlow, paymentsFlow) { tenant, payments ->
            if (tenant == null) {
                return@combine TenantDetailsState(
                    tenantId = tenantId,
                    profileState = UiState.Error("Tenant not found"),
                    leaseState = UiState.Error("Tenant not found"),
                    transactionsState = UiState.Error("Tenant not found")
                )
            }
            
            val profile = TenantProfileDisplayModel(
                id = tenant.id,
                name = tenant.name,
                address = tenant.propertyName ?: "Unknown Property",
                isVerified = true,
                avatarUrl = null,
                phone = tenant.phone,
                movedInDate = DateTimeUtil.formatReadableDate(tenant.createdAt)
            )
            
            val lease = TenantLeaseDisplayModel(
                monthlyRent = CurrencyUtil.formatNprLabel(tenant.rentAmount),
                status = tenant.status,
                isActive = tenant.status == "Active",
                startDate = DateTimeUtil.formatReadableDate(tenant.createdAt),
                endDate = "Ongoing"
            )
            
            val txs = payments.map {
                TenantTransactionDisplayModel(
                    id = it.id,
                    type = if (it.paymentMethod == "Deposit") "Deposit" else "Rent Payment",
                    date = DateTimeUtil.formatReadableDate(it.date),
                    amount = CurrencyUtil.formatNprLabel(it.amount),
                    status = it.status,
                    isPaid = it.status == "Paid",
                    method = it.paymentMethod
                )
            }.toImmutableList()

            TenantDetailsState(
                tenantId = tenantId,
                profileState = UiState.Success(profile),
                leaseState = UiState.Success(lease),
                transactionsState = UiState.Success(txs)
            )
        }.collectLatest { newState ->
            reduce { newState }
        }
    }

    override fun onAction(action: TenantDetailsAction) {
        intent {
            when (action) {
                is TenantDetailsAction.OnBackClicked -> {
                    postSideEffect(TenantDetailsEffect.NavigateBack)
                }
                is TenantDetailsAction.OnEditClicked -> {
                    postSideEffect(TenantDetailsEffect.NavigateToEdit(tenantId))
                }
                is TenantDetailsAction.OnPaymentClicked -> {
                    postSideEffect(TenantDetailsEffect.ShowToast("Payment clicked"))
                }
                is TenantDetailsAction.OnEmailClicked -> {
                    postSideEffect(TenantDetailsEffect.ShowToast("Email clicked"))
                }
                is TenantDetailsAction.OnCallClicked -> {
                    postSideEffect(TenantDetailsEffect.ShowToast("Call clicked"))
                }
                is TenantDetailsAction.OnMessageClicked -> {
                    postSideEffect(TenantDetailsEffect.ShowToast("Message clicked"))
                }
                is TenantDetailsAction.OnMaintenanceClicked -> {
                    postSideEffect(TenantDetailsEffect.ShowToast("Maintenance clicked"))
                }
                is TenantDetailsAction.OnViewAllTransactionsClicked -> {
                    postSideEffect(TenantDetailsEffect.ShowToast("View All clicked"))
                }
                is TenantDetailsAction.OnTransactionClicked -> {
                    postSideEffect(TenantDetailsEffect.ShowToast("Transaction ${action.transactionId} clicked"))
                }
                is TenantDetailsAction.OnDeleteClicked -> {
                    reduce { state.copy(showDeleteConfirm = true) }
                }
                is TenantDetailsAction.OnDeleteDismissed -> {
                    reduce { state.copy(showDeleteConfirm = false) }
                }
                is TenantDetailsAction.OnDeleteConfirmed -> {
                    handleDelete()
                }
            }
        }
    }

    private fun handleDelete() = intent {
        reduce { state.copy(isDeleting = true, showDeleteConfirm = false) }
        when (val result = tenantRepository.deleteTenant(tenantId)) {
            is ApiResponse.Success -> postSideEffect(TenantDetailsEffect.NavigateBack)
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isDeleting = false) }
                postSideEffect(TenantDetailsEffect.ShowError(
                    ErrorMessageExtractor.extract(result, "Failed to delete tenant")
                ))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isDeleting = false) }
                postSideEffect(TenantDetailsEffect.ShowError(
                    ErrorMessageExtractor.extract(result, "Failed to delete tenant")
                ))
            }
        }
    }
}
