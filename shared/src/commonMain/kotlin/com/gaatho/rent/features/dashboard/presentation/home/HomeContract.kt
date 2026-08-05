package com.gaatho.rent.features.dashboard.presentation.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HomeState(
    val userName: String = "",
    val greeting: String = "",
    val collectedRent: Long = 0L,
    val totalRent: Long = 0L,
    val outstandingRent: Long = 0L,
    val propertiesCount: Int = 0,
    val tenantsCount: Int = 0,
    val searchQuery: String = "",
    val overdueTenantsCount: Int = 0,
    val recentPayments: ImmutableList<RecentPaymentItem> = persistentListOf()
)

data class RecentPaymentItem(
    val tenantId: String,
    val tenantName: String,
    val propertyName: String, // E.g., Unit: 13
    val dateLabel: String,
    val amount: Long,
    val isPaid: Boolean
)

sealed interface HomeAction {
    data object OnAddTenantClicked : HomeAction
    data object OnAddPropertyClicked : HomeAction
    data object OnRecordPaymentClicked : HomeAction
    data object OnExpenseClicked : HomeAction
    data object OnSeeAllPaymentsClicked : HomeAction
    data class OnRecentPaymentClicked(val tenantId: String) : HomeAction
    data class OnSearchQueryChanged(val query: String) : HomeAction
}

sealed interface HomeSideEffect {
    data object NavigateToAddTenant : HomeSideEffect
    data object NavigateToAddProperty : HomeSideEffect
    data object NavigateToAddPayment : HomeSideEffect
    data object NavigateToPayments : HomeSideEffect
    data object NavigateToExpenses : HomeSideEffect
    data class NavigateToTenantDetails(val tenantId: String) : HomeSideEffect
}
