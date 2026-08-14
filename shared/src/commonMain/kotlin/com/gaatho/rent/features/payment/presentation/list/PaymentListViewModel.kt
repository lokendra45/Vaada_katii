package com.gaatho.rent.features.payment.presentation.list

import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PaymentListViewModel(
    private val paymentRepository: PaymentRepository,
    private val tenantRepository: TenantRepository,
    private val propertyRepository: PropertyRepository,
    private val userIdentityProvider: UserIdentityProvider
) : MviViewModel<PaymentListState, PaymentListSideEffect, PaymentListAction>() {

    private val ownerId: String
        get() = userIdentityProvider.currentUserId()

    /**
     * Search query lives in its own MutableStateFlow — NOT in Orbit state.
     *
     * NiA / Google pattern: keeping raw text field value separate avoids full
     * Orbit reduce() cycles on every keystroke. UI holds its own `var searchQuery`
     * and calls [onSearchQueryChanged] directly.
     */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /** Trigger for manual retries/refreshes. Emits Unit to restart the observation pipeline. */
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    override val container = orbitContainer<PaymentListState, PaymentListSideEffect>(
        initialState = PaymentListState()
    ) {
        observeData()
    }

    /**
     * Reactive data observation pipeline.
     * Combines refresh triggers, search query, and filters into a single stream.
     * flatMapLatest ensures that any change in filters or a retry cancels the previous
     * repository collection and starts a new one.
     */
    private val dataFlow: Flow<UiState<ImmutableList<PaymentDisplayModel>>> = combine(
        refreshTrigger,
        _searchQuery.debounce(300L.milliseconds).distinctUntilChanged(),
        container.stateFlow.map { it.selectedStatus }.distinctUntilChanged(),
        container.stateFlow.map { it.selectedMonth }.distinctUntilChanged()
    ) { _, query, status, month ->
        FilterParams(query, status, month)
    }.flatMapLatest { filters ->
        flow {
            emit(UiState.Loading)
            combine(
                paymentRepository.getPaymentsByOwner(ownerId),
                tenantRepository.getTenants(ownerId),
                propertyRepository.getProperties(ownerId)
            ) { payments, tenants, properties ->
                val processedPayments = payments.mapNotNull { payment ->
                    val tenant = tenants.find { it.id == payment.tenantId } ?: return@mapNotNull null
                    val property = properties.find { it.id == payment.propertyId } ?: return@mapNotNull null

                    PaymentDisplayModel(
                        id = payment.id,
                        tenantId = tenant.id,
                        tenantName = tenant.name,
                        propertyName = property.name,
                        date = payment.date,
                        dateLabel = DateTimeUtil.formatReadableDate(payment.date),
                        amount = payment.amount,
                        status = payment.status,
                        isPaid = payment.status == PaymentListFilters.Paid,
                        unit = tenant.roomNumber,
                        paymentMethod = payment.paymentMethod
                    )
                }.sortedByDescending { it.date }

                var filtered = processedPayments

                if (filters.query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.tenantName.contains(filters.query, ignoreCase = true) ||
                            it.propertyName.contains(filters.query, ignoreCase = true)
                    }
                }

                if (filters.status != PaymentListFilters.AllStatuses) {
                    filtered = filtered.filter { it.status.equals(filters.status, ignoreCase = true) }
                }

                if (filters.month != PaymentListFilters.AllMonths) {
                    filtered = filtered.filter { it.dateLabel.contains(filters.month, ignoreCase = true) }
                }

                filtered.toImmutableList()
            }.catch { e ->
                emit(UiState.Error(ErrorMessageExtractor.extract(e, "Failed to load payments")))
            }.collect {
                emit(UiState.Success(it))
            }
        }
    }

    private data class FilterParams(val query: String, val status: String, val month: String)

    private fun observeData() = intent {
        dataFlow.collect { newState ->
            reduce { state.copy(paymentsState = newState) }
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
            PaymentListAction.OnRetry -> refreshTrigger.tryEmit(Unit)
            is PaymentListAction.OnStatusFilterChanged -> intent {
                reduce { state.copy(selectedStatus = action.status) }
            }
            is PaymentListAction.OnMonthFilterChanged -> intent {
                reduce { state.copy(selectedMonth = action.month) }
            }
        }
    }
}
