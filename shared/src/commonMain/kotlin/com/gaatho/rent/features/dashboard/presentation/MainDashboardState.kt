package com.gaatho.rent.features.dashboard.presentation

import com.gaatho.rent.features.dashboard.presentation.model.DashboardTab
import kotlinx.serialization.Serializable

/**
 * State representing the Main Dashboard UI.
 * Must be @Serializable to be persisted across process death by Orbit MVI.
 */
@Serializable
data class MainDashboardState(
    val selectedTab: DashboardTab = DashboardTab.HOME,
    val isLoading: Boolean = false,
    val error: String? = null
)

/** Actions triggered by the dashboard UI. */
sealed interface MainDashboardAction {
    data class OnTabSelected(val tab: DashboardTab) : MainDashboardAction
}

/** One-off side effects emitted by the dashboard UI. */
sealed interface MainDashboardSideEffect {
    // Currently no side effects needed, but defined for strict MVI adherence
}
