package com.gaatho.rent.features.property.presentation.details

import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList


data class PropertyUnitsState(
    val propertyId: String = "",
    val unitsState: UiState<ImmutableList<UnitDisplayModel>> = UiState.Idle
)

sealed interface PropertyUnitsAction

sealed interface PropertyUnitsEffect
