package com.gaatho.rent.features.tenant.presentation.details

import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import kotlinx.coroutines.flow.collectLatest
import org.orbitmvi.orbit.viewmodel.orbitContainer
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.utils.CurrencyUtil
import kotlinx.collections.immutable.toImmutableList

class TenantTransactionsViewModel(
    private val tenantId: String,
    private val paymentRepository: PaymentRepository
) : MviViewModel<TenantTransactionsState, TenantTransactionsEffect, TenantTransactionsAction>() {

    override val container = orbitContainer<TenantTransactionsState, TenantTransactionsEffect>(
        initialState = TenantTransactionsState(tenantId = tenantId)
    ) {
        loadTransactions()
    }

    private fun loadTransactions() = intent(registerIdling = false) {
        paymentRepository.getPaymentsByTenant(tenantId).collectLatest { payments ->
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
            
            reduce { state.copy(transactionsState = UiState.Success(txs)) }
        }
    }

    override fun onAction(action: TenantTransactionsAction) {
        intent {
            when (action) {
                is TenantTransactionsAction.OnPaymentClicked -> {
                    postSideEffect(TenantTransactionsEffect.ShowToast("Payment clicked"))
                }
                is TenantTransactionsAction.OnViewAllTransactionsClicked -> {
                    postSideEffect(TenantTransactionsEffect.NavigateToTransactions(tenantId))
                }
                is TenantTransactionsAction.OnTransactionClicked -> {
                    postSideEffect(TenantTransactionsEffect.ShowToast("Transaction ${action.transactionId} clicked"))
                }
            }
        }
    }
}
