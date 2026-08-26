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

        // Fetch dashboard data
        intent {
            // Compute greeting based on local time
            val hour = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
            val greetingText = when (hour) {
                in 5..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                else -> "Good evening"
            }

            reduce { state.copy(greeting = greetingText) }

            val ownerId = (sessionManager.currentUserId() ?: "")

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
                    greeting = greetingText,
                    collectedRent = summary.collectedRent,
                    totalRent = summary.totalRent,
                    outstandingRent = summary.outstandingRent,
                    propertiesCount = summary.propertiesCount.toInt(),
                    tenantsCount = summary.tenantsCount.toInt(),
                    overdueTenantsCount = overdueCount,
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
                is HomeAction.OnRecordPaymentClicked -> postSideEffect(HomeSideEffect.NavigateToAddPayment)
                is HomeAction.OnExpenseClicked -> postSideEffect(HomeSideEffect.NavigateToExpenses)
                is HomeAction.OnSeeAllPaymentsClicked -> postSideEffect(HomeSideEffect.NavigateToPayments)
                is HomeAction.OnRecentPaymentClicked -> postSideEffect(HomeSideEffect.NavigateToTenantDetails(action.tenantId))
            }
        }
    }
}