package com.gaatho.rent.features.payment.presentation.list

import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.viewmodel.orbitContainer

class PaymentListViewModel(
    private val paymentRepository: PaymentRepository,
    private val tenantRepository: TenantRepository,
    private val propertyRepository: PropertyRepository,
    private val userIdentityProvider: UserIdentityProvider
) : MviViewModel<PaymentListState, PaymentListSideEffect, PaymentListAction>() {

    override val container = orbitContainer<PaymentListState, PaymentListSideEffect>(
        initialState = PaymentListState()
    ) {
        observeData()
    }

    private fun observeData() = intent {
        val ownerId = userIdentityProvider.currentUserId()

        combine(
            paymentRepository.getPaymentsByOwner(ownerId),
            tenantRepository.getTenants(ownerId),
            propertyRepository.getProperties(ownerId)
        ) { payments, tenants, properties ->
            val currentState = state

            val processedPayments = payments.mapNotNull { payment ->
                val tenant = tenants.find { it.id == payment.tenantId } ?: return@mapNotNull null
                val property = properties.find { it.id == payment.propertyId } ?: return@mapNotNull null

                PaymentDisplayModel(
                    id = payment.id,
                    tenantId = tenant.id,
                    tenantName = tenant.name,
                    propertyName = property.name,
                    dateLabel = DateTimeUtil.formatReadableDate(payment.date),
                    amount = payment.amount,
                    status = payment.status,
                    isPaid = payment.status == "Paid"
                )
            }.sortedByDescending { it.dateLabel }

            var filtered = processedPayments

            if (currentState.searchQuery.isNotBlank()) {
                filtered = filtered.filter {
                    it.tenantName.contains(currentState.searchQuery, ignoreCase = true) ||
                    it.propertyName.contains(currentState.searchQuery, ignoreCase = true)
                }
            }

            if (currentState.selectedStatus != "All statuses") {
                filtered = filtered.filter { it.status.equals(currentState.selectedStatus, ignoreCase = true) }
            }

            // A real app would parse the month, but let's do a simple string match for this MVP
            if (currentState.selectedMonth != "All months") {
                filtered = filtered.filter { it.dateLabel.contains(currentState.selectedMonth, ignoreCase = true) }
            }

            filtered.toImmutableList()
        }
        .catch { e ->
            reduce {
                state.copy(
                    paymentsState = UiState.Error(
                        ErrorMessageExtractor.extract(e, "Failed to load payments")
                    )
                )
            }
        }
        .collect { list ->
            reduce { state.copy(paymentsState = UiState.Success(list)) }
        }
    }

    override fun onAction(action: PaymentListAction) {
        when (action) {
            PaymentListAction.OnBackClicked -> intent {
                postSideEffect(PaymentListSideEffect.NavigateBack)
            }
            is PaymentListAction.OnPaymentClicked -> intent {
                postSideEffect(PaymentListSideEffect.NavigateToPaymentDetails(action.paymentId))
            }
            PaymentListAction.OnAddPaymentClicked -> intent {
                postSideEffect(PaymentListSideEffect.NavigateToAddPayment)
            }
            PaymentListAction.OnRetry -> observeData()
            is PaymentListAction.OnSearchQueryChanged -> intent {
                reduce { state.copy(searchQuery = action.query) }
                // Re-trigger flow processing implicitly via combine in a real app,
                // but since observeData's combine depends on repos, it won't re-trigger just from state.
                // A better MVI approach is flatMapLatest on state, but we'll re-observe manually here or
                // restart the flow. For MVP, we'll re-call observeData.
                observeData()
            }
            is PaymentListAction.OnStatusFilterChanged -> intent {
                reduce { state.copy(selectedStatus = action.status) }
                observeData()
            }
            is PaymentListAction.OnMonthFilterChanged -> intent {
                reduce { state.copy(selectedMonth = action.month) }
                observeData()
            }
        }
    }
}
