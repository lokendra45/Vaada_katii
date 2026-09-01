package com.gaatho.rent.features.dashboard.presentation.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class DashboardPeriod {
    THIS_MONTH,
    LAST_MONTH
}

data class HomeState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val userName: String = "",
    val greeting: String = "",
    val selectedPeriod: DashboardPeriod = DashboardPeriod.THIS_MONTH,
    val collectedRent: Long = 0L,
    val previousCollectedRent: Long = 0L,
    val totalRent: Long = 0L,
    val outstandingRent: Long = 0L,
    val propertiesCount: Int = 0,
    val tenantsCount: Int = 0,
    val overdueTenantsCount: Int = 0,
    val chartData: ImmutableList<Float> = persistentListOf(),
    val properties: ImmutableList<DashboardPropertyItem> = persistentListOf(),
    val recentPayments: ImmutableList<RecentPaymentItem> = persistentListOf()
)

data class DashboardPropertyItem(
    val id: String,
    val name: String,
    val location: String,
    val imageUrl: String? = null,
    val totalUnits: Int,
    val occupiedUnits: Int
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
    data object OnSeeAllPropertiesClicked : HomeAction
    data object OnExpenseClicked : HomeAction
    data object OnSeeAllPaymentsClicked : HomeAction
    data class OnRecentPaymentClicked(val tenantId: String) : HomeAction
    data class OnPeriodChanged(val period: DashboardPeriod) : HomeAction
}

sealed interface HomeSideEffect {
    data object NavigateToAddTenant : HomeSideEffect
    data object NavigateToAddProperty : HomeSideEffect
    data object NavigateToAddPayment : HomeSideEffect
    data object NavigateToProperties : HomeSideEffect
    data object NavigateToPayments : HomeSideEffect
    data object NavigateToExpenses : HomeSideEffect
    data class NavigateToTenantDetails(val tenantId: String) : HomeSideEffect
}
