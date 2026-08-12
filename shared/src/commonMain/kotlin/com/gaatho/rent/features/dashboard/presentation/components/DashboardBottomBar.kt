package com.gaatho.rent.features.dashboard.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.features.dashboard.presentation.model.DashboardTab
import com.gaatho.rent.features.dashboard.presentation.model.selectedIcon
import com.gaatho.rent.features.dashboard.presentation.model.unselectedIcon

/**
 * Reusable Bottom Navigation component for the Dashboard.
 * 
 * Extracts the Material 3 NavigationBar styling out of the main screen to adhere to
 * separation of concerns.
 */
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardBottomBar(
    currentTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .height(72.dp)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        DashboardTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = stringResource(tab.title)
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.EmeraldAccent,
                    selectedTextColor = AppColors.EmeraldAccent,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
