package com.gaatho.rent.features.property.presentation.details

import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import kotlinx.coroutines.flow.catch
import org.orbitmvi.orbit.viewmodel.orbitContainer

class PropertyIdentityViewModel(
    private val propertyId: String,
    private val propertyRepository: PropertyRepository,
) : MviViewModel<PropertyIdentityState, PropertyIdentityEffect, PropertyIdentityAction>() {

    override val container = orbitContainer<PropertyIdentityState, PropertyIdentityEffect>(
        initialState = PropertyIdentityState(propertyId = propertyId)
    ) {
        observeData()
    }

    private fun observeData() = intent {
        propertyRepository.getPropertyById(propertyId)
            .catch { e ->
                reduce { state.copy(propertyState = UiState.Error(ErrorMessageExtractor.extract(e, "Failed to load property"))) }
            }
            .collect { property ->
                reduce {
                    state.copy(
                        propertyState = if (property != null) UiState.Success(property)
                        else UiState.Error("Property not found")
                    )
                }
            }
    }

    override fun onAction(action: PropertyIdentityAction) {}
}
