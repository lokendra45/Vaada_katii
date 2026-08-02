package com.gaatho.rent.features.dashboard.presentation.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

import org.jetbrains.compose.resources.StringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*

/**
 * Defines the primary bottom navigation tabs for the landlord dashboard.
 * 
 * Used within Orbit MVI state to track the active tab.
 */
@Serializable
enum class DashboardTab(
    @Transient val title: StringResource = Res.string.tab_home // Needs a default for serialization, though @Transient ignores it
) {
    HOME(title = Res.string.tab_home),
    PROPERTIES(title = Res.string.tab_properties),
    PAYMENTS(title = Res.string.tab_payments),
    TENANTS(title = Res.string.tab_tenants),
    SETTINGS(title = Res.string.tab_settings);
    
    // We do not serialize ImageVector inside the Enum to avoid serialization issues, 
    // so we expose them via extension properties.
}

val DashboardTab.selectedIcon: ImageVector
    get() = when (this) {
        DashboardTab.HOME -> Icons.Filled.Home
        DashboardTab.PROPERTIES -> Icons.Filled.Domain
        DashboardTab.PAYMENTS -> Icons.Filled.Payments
        DashboardTab.TENANTS -> Icons.Filled.Group
        DashboardTab.SETTINGS -> Icons.Filled.Settings
    }

val DashboardTab.unselectedIcon: ImageVector
    get() = when (this) {
        DashboardTab.HOME -> Icons.Outlined.Home
        DashboardTab.PROPERTIES -> Icons.Outlined.Domain
        DashboardTab.PAYMENTS -> Icons.Outlined.Payments
        DashboardTab.TENANTS -> Icons.Outlined.Group
        DashboardTab.SETTINGS -> Icons.Outlined.Settings
    }
