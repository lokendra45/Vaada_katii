package com.gaatho.rent.features.dashboard.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gaatho.rent.core.ui.animation.tabSlideTransition
import com.gaatho.rent.features.dashboard.presentation.home.HomeScreen
import com.gaatho.rent.features.dashboard.presentation.model.DashboardTab
import com.gaatho.rent.features.property.presentation.list.PropertyListScreen
import com.gaatho.rent.features.payment.presentation.list.PaymentsListScreen
import com.gaatho.rent.features.settings.presentation.SettingsScreen
import com.gaatho.rent.features.tenant.presentation.list.TenantsListScreen

/**
 * Handles the switching of active tabs in the Main Dashboard.
 *
 * Uses [AnimatedContent] with spring-physics [tabSlideTransition] for a premium,
 * buttery-smooth feel — same animation system as GaathoMobileApp.
 *
 * [rememberSaveableStateHolder] preserves each tab's scroll position and form state
 * across tab switches so state is never lost on re-entry.
 */
@Composable
fun DashboardContent(
    currentTab: DashboardTab,
    modifier: Modifier = Modifier,
    onNavigateToPropertyDetails: (String) -> Unit,
    onNavigateToAddProperty: () -> Unit,
    onNavigateToTenantDetails: (String) -> Unit = {},
    onNavigateToAddTenant: () -> Unit = {},
    onNavigateToAddPayment: () -> Unit = {},
    onNavigateToPaymentDetails: (String) -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    content: @Composable (DashboardTab) -> Unit = { tab ->
        DefaultDashboardTabContent(
            tab = tab,
            onNavigateToPropertyDetails = onNavigateToPropertyDetails,
            onNavigateToAddProperty = onNavigateToAddProperty,
            onNavigateToTenantDetails = onNavigateToTenantDetails,
            onNavigateToAddTenant = onNavigateToAddTenant,
            onNavigateToAddPayment = onNavigateToAddPayment,
            onNavigateToPaymentDetails = onNavigateToPaymentDetails,
            onNavigateToPayments = onNavigateToPayments,
            onNavigateToLogin = onNavigateToLogin
        )
    }
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentTab,
            label = "DashboardTabTransition",
            transitionSpec = {
                val toIndex = DashboardTab.entries.indexOf(targetState)
                val fromIndex = DashboardTab.entries.indexOf(initialState)
                val direction = if (toIndex >= fromIndex) 1 else -1
                tabSlideTransition(direction = direction)
            }
        ) { tab ->
            saveableStateHolder.SaveableStateProvider(key = tab.ordinal) {
                content(tab)
            }
        }
    }
}

@Composable
private fun DefaultDashboardTabContent(
    tab: DashboardTab,
    onNavigateToPropertyDetails: (String) -> Unit,
    onNavigateToAddProperty: () -> Unit,
    onNavigateToTenantDetails: (String) -> Unit,
    onNavigateToAddTenant: () -> Unit,
    onNavigateToAddPayment: () -> Unit,
    onNavigateToPaymentDetails: (String) -> Unit,
    onNavigateToPayments: () -> Unit = {},
    onNavigateToLogin: () -> Unit
) {
    when (tab) {
        DashboardTab.HOME -> HomeScreen(
            onNavigateToAddTenant = onNavigateToAddTenant,
            onNavigateToAddProperty = onNavigateToAddProperty,
            onNavigateToAddPayment = onNavigateToAddPayment,
            onNavigateToPayments = onNavigateToPayments,
            onNavigateToTenantDetails = onNavigateToTenantDetails
        )
        DashboardTab.PROPERTIES -> PropertyListScreen(
            onNavigateToDetails = onNavigateToPropertyDetails,
            onNavigateToAddProperty = onNavigateToAddProperty
        )
        DashboardTab.PAYMENTS -> PaymentsListScreen(
            onNavigateToAddPayment = onNavigateToAddPayment,
            onNavigateToDetails = onNavigateToPaymentDetails
        )
        DashboardTab.TENANTS -> TenantsListScreen(
            onNavigateToDetails = onNavigateToTenantDetails,
            onNavigateToAddTenant = onNavigateToAddTenant
        )
        DashboardTab.SETTINGS -> SettingsScreen(
            onNavigateToLogin = onNavigateToLogin
        )
    }
}

@Composable
private fun PlaceholderTab(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
