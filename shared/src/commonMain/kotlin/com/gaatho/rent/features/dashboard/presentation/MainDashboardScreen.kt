package com.gaatho.rent.features.dashboard.presentation

import androidx.compose.foundation.layout.padding
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
    onNavigateToAddProperty: () -> Unit
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
        }
    ) { innerPadding ->
        DashboardContent(
            currentTab = state.selectedTab,
            modifier = Modifier.padding(innerPadding),
            onNavigateToPropertyDetails = onNavigateToPropertyDetails,
            onNavigateToAddProperty = onNavigateToAddProperty
        )
    }
}
