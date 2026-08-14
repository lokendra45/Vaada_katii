package com.gaatho.rent.features.property.presentation.details

import com.gaatho.rent.core.ui.UiState

data class PropertyStatsState(
    val propertyId: String = "",
    val financialState: UiState<FinancialSummary> = UiState.Idle,
    val monthlyIncome: Long = 0,
    val totalUnits: Int = 0,
    val occupiedUnits: Int = 0
)

sealed interface PropertyStatsAction

sealed interface PropertyStatsEffect
