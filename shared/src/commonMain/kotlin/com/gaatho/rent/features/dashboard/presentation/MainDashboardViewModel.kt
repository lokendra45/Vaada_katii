package com.gaatho.rent.features.dashboard.presentation

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.features.dashboard.presentation.model.DashboardTab
import org.orbitmvi.orbit.viewmodel.orbitContainer

/**
 * ViewModel managing the state of the Main Dashboard.
 * 
 * Uses Orbit MVI to maintain the active tab and persists it across process death
 * via [SavedStateHandle].
 */
class MainDashboardViewModel(
    savedStateHandle: SavedStateHandle
) : MviViewModel<MainDashboardState, MainDashboardSideEffect, MainDashboardAction>() {

    override val container = orbitContainer<MainDashboardState, MainDashboardSideEffect>(
        initialState = MainDashboardState(),
        savedStateHandle = savedStateHandle,
        serializer = MainDashboardState.serializer()
    )

    override fun onAction(action: MainDashboardAction) {
        when (action) {
            is MainDashboardAction.OnTabSelected -> handleTabSelected(action.tab)
        }
    }

    private fun handleTabSelected(tab: DashboardTab) = intent {
        reduce {
            state.copy(selectedTab = tab)
        }
    }
}
