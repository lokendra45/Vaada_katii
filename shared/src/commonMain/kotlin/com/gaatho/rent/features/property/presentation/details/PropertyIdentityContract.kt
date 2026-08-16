package com.gaatho.rent.features.property.presentation.details

import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.domain.model.Property

data class PropertyIdentityState(
    val propertyId: String = "",
    val propertyState: UiState<Property> = UiState.Idle
)

sealed interface PropertyIdentityAction

sealed interface PropertyIdentityEffect
