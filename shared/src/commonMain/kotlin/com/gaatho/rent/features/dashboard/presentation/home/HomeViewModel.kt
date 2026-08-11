package com.gaatho.rent.features.dashboard.presentation.home

import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.viewmodel.orbitContainer

class HomeViewModel(
    private val userIdentityProvider: UserIdentityProvider,
    private val tenantRepository: TenantRepository,
    private val propertyRepository: PropertyRepository,
    private val paymentRepository: PaymentRepository
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

        val propertiesFlow = propertyRepository.getProperties(ownerId)
        val tenantsFlow = tenantRepository.getTenants(ownerId)
        val paymentsFlow = paymentRepository.getPaymentsByOwner(ownerId)

        combine(propertiesFlow, tenantsFlow, paymentsFlow) { properties, tenants, payments ->
            val activeTenants = tenants.filter { it.status == "Active" }
            val totalRent = activeTenants.sumOf { it.rentAmount }

            val collectedRent = payments.filter { it.status == "Paid" }.sumOf { it.amount }
            val outstandingRent = maxOf(0L, totalRent - collectedRent)

            val recentPayments = payments
                .sortedByDescending { it.date }
                .take(5)
                .map { payment ->
                    val tenant = tenants.find { it.id == payment.tenantId }
                    RecentPaymentItem(
                        tenantId = payment.tenantId,
                        tenantName = tenant?.name ?: "Unknown Tenant",
                        propertyName = tenant?.roomNumber ?: tenant?.propertyName ?: "Unknown Unit",
                        dateLabel = DateTimeUtil.formatReadableDate(payment.date),
                        amount = payment.amount,
                        isPaid = payment.status == "Paid"
                    )
                }.toImmutableList()

            HomeState(
                isLoading = false,
                userName = userName,
                greeting = greetingText,
                collectedRent = collectedRent,
                totalRent = totalRent,
                outstandingRent = outstandingRent,
                propertiesCount = properties.size,
                tenantsCount = activeTenants.size,
                overdueTenantsCount = tenants.count { it.status == "Overdue" },
                recentPayments = recentPayments
            )
        }.collectLatest { newState ->
            reduce { newState }
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
