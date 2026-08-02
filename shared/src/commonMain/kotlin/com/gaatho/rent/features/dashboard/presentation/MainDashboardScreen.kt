package com.gaatho.rent.features.dashboard.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.gaatho.rent.features.dashboard.presentation.components.DashboardBottomBar
import com.gaatho.rent.features.dashboard.presentation.components.DashboardContent
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
    onNavigateToLogin: () -> Unit = {}
) {
    val viewModel: MainDashboardViewModel = koinViewModel()
    val state by viewModel.collectAsState()

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = state.selectedTab,
                onTabSelected = { tab ->
                    viewModel.onAction(MainDashboardAction.OnTabSelected(tab))
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        DashboardContent(
            currentTab = state.selectedTab,
            modifier = Modifier.padding(innerPadding),
            onNavigateToPropertiesTab = {
                viewModel.onAction(MainDashboardAction.OnTabSelected(com.gaatho.rent.features.dashboard.presentation.model.DashboardTab.PROPERTIES))
            },
            onNavigateToPropertyDetails = onNavigateToPropertyDetails,
            onNavigateToAddProperty = onNavigateToAddProperty,
            onNavigateToTenantDetails = onNavigateToTenantDetails,
            onNavigateToAddTenant = onNavigateToAddTenant,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}
