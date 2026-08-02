package com.gaatho.rent.features.tenant.presentation.details

import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import kotlinx.coroutines.delay
import kotlinx.collections.immutable.persistentListOf
import org.orbitmvi.orbit.viewmodel.orbitContainer

class TenantDetailsViewModel(
    private val tenantId: String
) : MviViewModel<TenantDetailsState, TenantDetailsEffect, TenantDetailsAction>() {

    override val container = orbitContainer<TenantDetailsState, TenantDetailsEffect>(
        initialState = TenantDetailsState(tenantId = tenantId)
    ) {
        loadMockData()
    }

    private fun loadMockData() = intent {
        reduce {
            state.copy(
                profileState = UiState.Loading,
                leaseState = UiState.Loading,
                transactionsState = UiState.Loading
            )
        }

        // Simulate network delay
        delay(800)

        reduce {
            state.copy(
                profileState = UiState.Success(
                    TenantProfileDisplayModel(
                        id = tenantId,
                        name = "Suman Shrestha",
                        address = "Bakhundole, Lalitpur",
                        isVerified = true,
                        avatarUrl = null
                    )
                ),
                leaseState = UiState.Success(
                    TenantLeaseDisplayModel(
                        monthlyRent = "Rs. 45,000",
                        status = "Active",
                        isActive = true,
                        startDate = "Sept 1, 2023",
                        endDate = "Aug 31, 2024"
                    )
                ),
                transactionsState = UiState.Success(
                    persistentListOf(
                        TenantTransactionDisplayModel(
                            id = "t1",
                            type = "Rent Payment",
                            date = "Nov 1, 2023",
                            amount = "Rs. 45,000",
                            status = "Paid",
                            isPaid = true
                        ),
                        TenantTransactionDisplayModel(
                            id = "t2",
                            type = "Rent Payment",
                            date = "Oct 1, 2023",
                            amount = "Rs. 45,000",
                            status = "Paid",
                            isPaid = true
                        ),
                        TenantTransactionDisplayModel(
                            id = "t3",
                            type = "Maintenance Fee",
                            date = "Sep 15, 2023",
                            amount = "Rs. 2,500",
                            status = "Paid",
                            isPaid = true
                        )
                    )
                )
            )
        }
    }

    override fun onAction(action: TenantDetailsAction) {
        intent {
            when (action) {
                is TenantDetailsAction.OnBackClicked -> {
                    postSideEffect(TenantDetailsEffect.NavigateBack)
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
            }
        }
    }
}
