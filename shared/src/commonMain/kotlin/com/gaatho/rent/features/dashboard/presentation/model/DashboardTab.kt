package com.gaatho.rent.features.dashboard.presentation.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/**
 * Defines the primary bottom navigation tabs for the landlord dashboard.
 * 
 * Used within Orbit MVI state to track the active tab.
 */
@Serializable
enum class DashboardTab(
    val title: String
) {
    HOME(title = "Home"),
    PROPERTIES(title = "Properties"),
    TENANTS(title = "Tenants"),
    SETTINGS(title = "Settings");
    
    // We do not serialize ImageVector inside the Enum to avoid serialization issues, 
    // so we expose them via extension properties.
}

val DashboardTab.selectedIcon: ImageVector
    get() = when (this) {
        DashboardTab.HOME -> Icons.Filled.Home
        DashboardTab.PROPERTIES -> Icons.Filled.Domain
        DashboardTab.TENANTS -> Icons.Filled.Group
        DashboardTab.SETTINGS -> Icons.Filled.Settings
    }

val DashboardTab.unselectedIcon: ImageVector
    get() = when (this) {
        DashboardTab.HOME -> Icons.Outlined.Home
        DashboardTab.PROPERTIES -> Icons.Outlined.Domain
        DashboardTab.TENANTS -> Icons.Outlined.Group
        DashboardTab.SETTINGS -> Icons.Outlined.Settings
    }
