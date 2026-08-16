package com.gaatho.rent.features.dashboard.presentation.home

import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.dashboard.data.DashboardRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.viewmodel.orbitContainer

class HomeViewModel(
    private val userIdentityProvider: UserIdentityProvider,
    private val dashboardRepository: DashboardRepository
) : MviViewModel<HomeState, HomeSideEffect, HomeAction>() {

    override val container = orbitContainer<HomeState, HomeSideEffect>(HomeState()) {
        observeData()
    }

    private fun observeData() = intent {
        // Compute greeting based on local time
        val hour = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        val greetingText = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }

        reduce { state.copy(greeting = greetingText) }

        // Use UserIdentityProvider — works for both guests and paid users.
        // Guest users get their stable local UUID; paid users get their Supabase UUID.
        val ownerId = userIdentityProvider.currentUserId()
        val userName = if (userIdentityProvider.isGuest()) "Guest" else "User"

        dashboardRepository.getDashboardSummary(ownerId).collectLatest { summary ->
            val recentPayments = summary.recentPayments
                .map { payment ->
                    RecentPaymentItem(
                        tenantId = payment.tenantId.orEmpty(),
                        tenantName = payment.tenantName ?: "Unknown Tenant",
                        propertyName = payment.unitNumber ?: "Unknown Unit",
                        dateLabel = DateTimeUtil.formatReadableDate(payment.date),
                        amount = payment.amount,
                        isPaid = payment.isPaid
                    )
                }.toImmutableList()

            reduce {
                state.copy(
                    isLoading = false,
                    userName = userName,
                    greeting = greetingText,
                    collectedRent = summary.collectedRent,
                    totalRent = summary.totalRent,
                    outstandingRent = summary.outstandingRent,
                    propertiesCount = summary.propertiesCount.toInt(),
                    tenantsCount = summary.tenantsCount.toInt(),
                    overdueTenantsCount = summary.overdueTenantsCount.toInt(),
                    recentPayments = recentPayments
                )
            }
        }
    }

    override fun onAction(action: HomeAction) {
        intent {
            when (action) {
                is HomeAction.OnAddTenantClicked -> postSideEffect(HomeSideEffect.NavigateToAddTenant)
                is HomeAction.OnAddPropertyClicked -> postSideEffect(HomeSideEffect.NavigateToAddProperty)
                is HomeAction.OnRecordPaymentClicked -> postSideEffect(HomeSideEffect.NavigateToAddPayment)
                is HomeAction.OnExpenseClicked -> postSideEffect(HomeSideEffect.NavigateToExpenses)
                is HomeAction.OnSeeAllPaymentsClicked -> postSideEffect(HomeSideEffect.NavigateToPayments)
                is HomeAction.OnRecentPaymentClicked -> postSideEffect(HomeSideEffect.NavigateToTenantDetails(action.tenantId))
            }
        }
    }
}