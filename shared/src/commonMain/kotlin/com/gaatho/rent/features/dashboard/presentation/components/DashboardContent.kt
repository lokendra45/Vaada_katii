package com.gaatho.rent.features.dashboard.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gaatho.rent.features.dashboard.presentation.model.DashboardTab
import com.gaatho.rent.features.property.presentation.list.PropertyListScreen

/**
 * Handles the switching of active tabs in the Main Dashboard.
 * 
 * In a complex app, each of these branches would route to a fully separated screen composable.
 */
@Composable
fun DashboardContent(
    currentTab: DashboardTab,
    modifier: Modifier = Modifier,
    onNavigateToPropertyDetails: (String) -> Unit,
    onNavigateToAddProperty: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (currentTab) {
            DashboardTab.HOME -> {
                PlaceholderTab("Home Overview Dashboard")
            }
            DashboardTab.PROPERTIES -> {
                // The fully built PropertyListScreen
                PropertyListScreen(
                    onNavigateToDetails = onNavigateToPropertyDetails,
                    onNavigateToAddProperty = onNavigateToAddProperty
                )
            }
            DashboardTab.TENANTS -> {
                PlaceholderTab("Tenants Management")
            }
            DashboardTab.SETTINGS -> {
                PlaceholderTab("App Settings & Profile")
            }
        }
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
