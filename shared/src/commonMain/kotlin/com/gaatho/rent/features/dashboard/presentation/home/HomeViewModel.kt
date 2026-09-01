package com.gaatho.rent.features.dashboard.presentation.home

import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.dashboard.data.DashboardRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.viewmodel.orbitContainer

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.gaatho.rent.core.notifications.NotificationService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.lifecycle.viewModelScope

class HomeViewModel(
    private val sessionManager: SessionManager,
    private val dashboardRepository: DashboardRepository,
    private val notificationService: NotificationService,
    private val dataStore: DataStore<Preferences>
) : MviViewModel<HomeState, HomeSideEffect, HomeAction>() {

    private val KEY_NOTIFICATIONS = booleanPreferencesKey("pref_notifications")
    private var hasShownOverdueNotification = false

    override val container = orbitContainer<HomeState, HomeSideEffect>(HomeState()) {
        observeData()
    }

    private var dataFetchJob: kotlinx.coroutines.Job? = null

    private fun observeData() {
        // Reactively observe user info to update name
        intent {
            sessionManager.authState.collectLatest { currentAuthState ->
                val user = when (currentAuthState) {
                    is com.gaatho.rent.core.auth.AuthState.Authenticated -> currentAuthState.user
                    is com.gaatho.rent.core.auth.AuthState.Anonymous -> currentAuthState.user
                    else -> null
                }
                
                val email = user?.email ?: ""
                val displayName = user?.displayName
                    ?: email.substringBefore("@").replaceFirstChar { it.uppercaseChar() }
                
                val isGuest = currentAuthState is com.gaatho.rent.core.auth.AuthState.Anonymous
                val userName = if (isGuest) "Guest" else displayName.ifBlank { "User" }

                reduce { state.copy(userName = userName) }
            }
        }

        // Fetch dashboard data reactively based on period changes
        intent {
            container.stateFlow
                .map { it.selectedPeriod }
                .distinctUntilChanged()
                .collectLatest { period ->
                    fetchDashboardData(period)
                }
        }
    }

    private fun fetchDashboardData(period: DashboardPeriod) = intent {
        // Compute greeting based on local time
        val greetingText = DateTimeUtil.getGreeting()
        reduce { 
            state.copy(
                greeting = greetingText, 
                isLoading = state.chartData.isEmpty() && state.collectedRent == 0L,
                isRefreshing = state.chartData.isNotEmpty() || state.collectedRent != 0L
            ) 
        }

        val ownerId = (sessionManager.currentUserId() ?: "")
        if (ownerId.isBlank()) return@intent

        val monthsAgo = if (period == DashboardPeriod.THIS_MONTH) 0 else 1
        val (start, end) = DateTimeUtil.getMonthsAgoDates(monthsAgo)
        val (prevStart, prevEnd) = DateTimeUtil.getMonthsAgoDates(monthsAgo + 1)

        dataFetchJob?.cancel()
        dataFetchJob = viewModelScope.launch {
            dashboardRepository.getDashboardSummary(
                ownerId = ownerId,
                startDate = start,
                endDate = end,
                prevStartDate = prevStart,
                prevEndDate = prevEnd
            ).collectLatest { summary ->
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
                    
                val properties = summary.recentProperties
                    .map { prop ->
                        DashboardPropertyItem(
                            id = prop.id,
                            name = prop.name,
                            location = prop.location,
                            imageUrl = prop.imageUrl,
                            totalUnits = prop.totalUnits,
                            occupiedUnits = prop.occupiedUnits
                        )
                    }.toImmutableList()
                    
                val overdueCount = summary.overdueTenantsCount.toInt()
                
                if (overdueCount > 0 && !hasShownOverdueNotification) {
                    val prefs = dataStore.data.first()
                    val notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: true
                    if (notificationsEnabled) {
                        notificationService.showNotification(
                            title = "Rent Overdue",
                            message = "You have $overdueCount tenant(s) with overdue rent."
                        )
                        hasShownOverdueNotification = true
                    }
                }

                reduce {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        greeting = greetingText,
                        collectedRent = summary.collectedRent,
                        previousCollectedRent = summary.previousCollectedRent,
                        totalRent = summary.totalRent,
                        outstandingRent = summary.outstandingRent,
                        propertiesCount = summary.propertiesCount.toInt(),
                        tenantsCount = summary.tenantsCount.toInt(),
                        overdueTenantsCount = overdueCount,
                        chartData = summary.chartData.toImmutableList(),
                        properties = properties,
                        recentPayments = recentPayments
                    )
                }
            }
        }
    }

    override fun onAction(action: HomeAction) {
        intent {
            when (action) {
                is HomeAction.OnAddTenantClicked -> postSideEffect(HomeSideEffect.NavigateToAddTenant)
                is HomeAction.OnAddPropertyClicked -> postSideEffect(HomeSideEffect.NavigateToAddProperty)
                is HomeAction.OnSeeAllPropertiesClicked -> postSideEffect(HomeSideEffect.NavigateToProperties)
                is HomeAction.OnRecordPaymentClicked -> postSideEffect(HomeSideEffect.NavigateToAddPayment)
                is HomeAction.OnExpenseClicked -> postSideEffect(HomeSideEffect.NavigateToExpenses)
                is HomeAction.OnSeeAllPaymentsClicked -> postSideEffect(HomeSideEffect.NavigateToPayments)
                is HomeAction.OnRecentPaymentClicked -> postSideEffect(HomeSideEffect.NavigateToTenantDetails(action.tenantId))
                is HomeAction.OnPeriodChanged -> reduce { state.copy(selectedPeriod = action.period) }
            }
        }
    }
}