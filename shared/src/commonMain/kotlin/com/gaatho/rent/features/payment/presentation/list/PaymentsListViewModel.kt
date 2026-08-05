package com.gaatho.rent.features.payment.presentation.list

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.viewmodel.orbitContainer

class PaymentsListViewModel(
    private val paymentRepository: PaymentRepository,
    private val tenantRepository: TenantRepository,
    private val propertyRepository: PropertyRepository,
    private val userIdentityProvider: UserIdentityProvider,
    savedStateHandle: SavedStateHandle
) : MviViewModel<PaymentsListState, PaymentsListSideEffect, PaymentsListAction>() {

    private val ownerId: String
        get() = userIdentityProvider.currentUserId()

    override val container = orbitContainer<PaymentsListState, PaymentsListSideEffect>(
        initialState = PaymentsListState(),
        savedStateHandle = savedStateHandle,
        serializer = PaymentsListState.serializer()
    ) {
        observeData()
    }

    override fun onAction(action: PaymentsListAction) {
        when (action) {
            is PaymentsListAction.OnSearchQueryChanged -> intent {
                val newState = state.copy(searchQuery = action.query)
                reduce { newState.copy(filteredPayments = computeFilteredPayments(newState)) }
            }
            is PaymentsListAction.OnStatusFilterChanged -> intent {
                val newState = state.copy(selectedStatus = action.status)
                reduce { newState.copy(filteredPayments = computeFilteredPayments(newState)) }
            }
            is PaymentsListAction.OnPropertyFilterChanged -> intent {
                val newState = state.copy(selectedProperty = action.property)
                reduce { newState.copy(filteredPayments = computeFilteredPayments(newState)) }
            }
            is PaymentsListAction.OnPaymentClicked -> intent {
                postSideEffect(PaymentsListSideEffect.NavigateToPaymentDetails(action.paymentId))
            }
            is PaymentsListAction.OnAddPayment -> intent {
                postSideEffect(PaymentsListSideEffect.NavigateToAddPayment)
            }
            is PaymentsListAction.OnRetry -> observeData()
        }
    }

    private fun observeData() = intent(registerIdling = false) {
        reduce { state.copy(paymentsState = UiState.Loading) }

        val paymentsFlow = paymentRepository.getPaymentsByOwner(ownerId)
        val tenantsFlow = tenantRepository.getTenants(ownerId)
        val propertiesFlow = propertyRepository.getProperties(ownerId)

        combine(paymentsFlow, tenantsFlow, propertiesFlow) { payments: List<Payment>, tenants: List<Tenant>, properties: List<Property> ->
            Triple(payments, tenants, properties)
        }
        .catch { e ->
            val msg = ErrorMessageExtractor.extract(e, "Could not load payments. Please try again.")
            reduce { state.copy(paymentsState = UiState.Error(msg)) }
        }
        .collect { (payments, tenants, properties) ->
            reduce { state.copy(propertiesState = UiState.Success(properties.toImmutableList())) }
            reduce { state.copy(tenantsState = UiState.Success(tenants.toImmutableList())) }

            val displayModels = payments.map { payment ->
                val tenant = tenants.find { it.id == payment.tenantId }
                val property = properties.find { it.id == payment.propertyId }
                
                PaymentDisplayModel(
                    id = payment.id,
                    tenantName = tenant?.name ?: "Unknown Tenant",
                    propertyName = property?.name ?: "Unknown Property",
                    amountFormatted = "Rs ${payment.amount}",
                    dateFormatted = payment.date.take(10),
                    status = payment.status,
                    paymentMethod = payment.paymentMethod ?: "Cash"
                )
            }.sortedByDescending { it.dateFormatted }.toImmutableList()

            val newState = state.copy(paymentsState = UiState.Success(displayModels))
            reduce { newState.copy(filteredPayments = computeFilteredPayments(newState)) }
        }
    }

    private fun computeFilteredPayments(s: PaymentsListState): ImmutableList<PaymentDisplayModel> {
        val raw = (s.paymentsState as? UiState.Success)?.data ?: return persistentListOf()
        return raw.filter { payment ->
            val matchesSearch = if (s.searchQuery.isBlank()) {
                true
            } else {
                val q = s.searchQuery.trim().lowercase()
                payment.tenantName.lowercase().contains(q) ||
                    payment.propertyName.lowercase().contains(q)
            }
            val matchesStatus = when (s.selectedStatus) {
                "All statuses" -> true
                else -> payment.status.equals(s.selectedStatus, ignoreCase = true)
            }
            val matchesProperty = when (s.selectedProperty) {
                "All properties" -> true
                else -> payment.propertyName.equals(s.selectedProperty, ignoreCase = true)
            }
            matchesSearch && matchesStatus && matchesProperty
        }.toImmutableList()
    }
}
