package com.gaatho.rent.features.dashboard.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.features.dashboard.presentation.components.DashboardBottomBar
import com.gaatho.rent.features.dashboard.presentation.components.DashboardContent
import com.gaatho.rent.features.dashboard.presentation.model.DashboardTab
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

/**
 * The main structural shell of the application after login.
 *
 * Uses Orbit MVI to maintain the active tab and delegates the rendering
 * to separated components ([DashboardBottomBar] and [DashboardContent])
 * for a cleaner architecture and better separation of concerns.
 */
@Composable
fun MainDashboardScreen(
    onNavigateToPropertyDetails: (String) -> Unit,
    onNavigateToAddProperty: () -> Unit,
    onNavigateToTenantDetails: (String) -> Unit = {},
    onNavigateToAddTenant: () -> Unit = {},
    onNavigateToAddPayment: () -> Unit = {},
    onNavigateToPaymentDetails: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val viewModel: MainDashboardViewModel = koinViewModel()
    val state by viewModel.collectAsState()

    MainDashboardContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToPropertyDetails = onNavigateToPropertyDetails,
        onNavigateToAddProperty = onNavigateToAddProperty,
        onNavigateToTenantDetails = onNavigateToTenantDetails,
        onNavigateToAddTenant = onNavigateToAddTenant,
        onNavigateToAddPayment = onNavigateToAddPayment,
        onNavigateToPaymentDetails = onNavigateToPaymentDetails,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun MainDashboardContent(
    state: MainDashboardState,
    onAction: (MainDashboardAction) -> Unit,
    onNavigateToPropertyDetails: (String) -> Unit,
    onNavigateToAddProperty: () -> Unit,
    onNavigateToTenantDetails: (String) -> Unit = {},
    onNavigateToAddTenant: () -> Unit,
    onNavigateToAddPayment: () -> Unit,
    onNavigateToPaymentDetails: (String) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    tabContent: @Composable (DashboardTab) -> Unit = { tab ->
        DashboardContent(
            currentTab = tab,
            onNavigateToPropertyDetails = onNavigateToPropertyDetails,
            onNavigateToAddProperty = onNavigateToAddProperty,
            onNavigateToTenantDetails = onNavigateToTenantDetails,
            onNavigateToAddTenant = onNavigateToAddTenant,
            onNavigateToAddPayment = onNavigateToAddPayment,
            onNavigateToPaymentDetails = onNavigateToPaymentDetails,
            onNavigateToPayments = { onAction(MainDashboardAction.OnTabSelected(DashboardTab.PAYMENTS)) },
            onNavigateToLogin = onNavigateToLogin
        )
    }
) {
    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = state.selectedTab,
                onTabSelected = { tab ->
                    onAction(MainDashboardAction.OnTabSelected(tab))
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            tabContent(state.selectedTab)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun MainDashboardScreenPreview() {
    RentManagerTheme {
        MainDashboardContent(
            state = MainDashboardState(selectedTab = DashboardTab.HOME),
            onAction = {},
            onNavigateToPropertyDetails = {},
            onNavigateToAddProperty = {},
            onNavigateToTenantDetails = {},
            onNavigateToAddTenant = {},
            onNavigateToAddPayment = {},
            onNavigateToPaymentDetails = {},
            onNavigateToLogin = {},
            tabContent = { tab ->
                com.gaatho.rent.features.dashboard.presentation.home.HomeScreen()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainDashboardScreenDarkPreview() {
    RentManagerTheme(darkTheme = true) {
        MainDashboardContent(
            state = MainDashboardState(selectedTab = DashboardTab.HOME),
            onAction = {},
            onNavigateToPropertyDetails = {},
            onNavigateToAddProperty = {},
            onNavigateToTenantDetails = {},
            onNavigateToAddTenant = {},
            onNavigateToAddPayment = {},
            onNavigateToPaymentDetails = {},
            onNavigateToLogin = {},
            tabContent = {
                com.gaatho.rent.features.dashboard.presentation.home.HomeScreen()
            }
        )
    }
}
