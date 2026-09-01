package com.gaatho.rent.features.payment.presentation.list

import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.utils.CurrencyUtil
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.viewmodel.orbitContainer
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.core.network.connectivity.ConnectivityObserver

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PaymentListViewModel(
    private val paymentRepository: PaymentRepository,
    private val sessionManager: SessionManager,
    private val connectivityObserver: ConnectivityObserver
) : MviViewModel<PaymentListState, PaymentListSideEffect, PaymentListAction>() {

    private val ownerId: String
        get() = (sessionManager.currentUserId() ?: "")

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchText.value = query
    }

    override val container = orbitContainer<PaymentListState, PaymentListSideEffect>(
        initialState = PaymentListState()
    ) {
        observeNetwork()
    }
    
    private fun observeNetwork() = intent(registerIdling = false) {
        connectivityObserver.isConnected.collect { isOnline ->
            reduce { state.copy(isOnline = isOnline) }
        }
    }

    /**
     * Single paginated flow. All filtering happens in SQL via the DAO query.
     * No more 3-way combine loading entire tables into memory.
     */
    val pagedPaymentsFlow: Flow<PagingData<PaymentDisplayModel>> = combine(
        _searchText
            .debounce(500L)
            .onEach { _isSearching.value = true }
            .distinctUntilChanged(),
        container.stateFlow
            .map { it.selectedStatus }
            .distinctUntilChanged()
    ) { search, status ->
        Pair(search, status)
    }.flatMapLatest { (search, status) ->
        val statusFilter = if (status == PaymentListFilters.AllStatuses) "" else status

        paymentRepository.getPagedPayments(
            ownerId = ownerId,
            searchQuery = search,
            statusFilter = statusFilter
        ).map { pagingData ->
            _isSearching.value = false
            pagingData.map { payment -> mapToDisplayModel(payment) }
        }.catch { e ->
            _isSearching.value = false
            AppLogger.network.e(e) { "Payments paging flow failed" }
            emit(PagingData.empty())
        }
    }.cachedIn(viewModelScope)

    private fun mapToDisplayModel(payment: Payment): PaymentDisplayModel {
        return PaymentDisplayModel(
            id = payment.id,
            tenantId = payment.tenantId,
            tenantName = payment.tenantName ?: "Unknown",
            propertyName = payment.propertyName ?: "Unknown",
            date = payment.date,
            dateLabel = DateTimeUtil.formatReadableDate(payment.date),
            amount = payment.amount,
            formattedAmount = "NPR ${CurrencyUtil.formatNpr(payment.amount.toDouble(), includeSymbol = false)}",
            status = payment.status,
            isPaid = payment.status == PaymentListFilters.Paid,
            unit = payment.roomNumber,
            paymentMethod = payment.paymentMethod
        )
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
            PaymentListAction.OnRetry -> { /* PagingData handles retry via LazyPagingItems.retry() */ }
            is PaymentListAction.OnStatusFilterChanged -> intent {
                reduce { state.copy(selectedStatus = action.status) }
            }
            is PaymentListAction.OnMonthFilterChanged -> intent {
                reduce { state.copy(selectedMonth = action.month) }
            }
        }
    }
}
