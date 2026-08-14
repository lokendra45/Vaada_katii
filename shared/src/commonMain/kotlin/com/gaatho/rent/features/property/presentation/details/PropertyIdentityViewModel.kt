package com.gaatho.rent.features.property.presentation.details

import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.viewmodel.orbitContainer

class PropertyIdentityViewModel(
    private val propertyId: String,
    private val propertyRepository: PropertyRepository,
    private val userIdentityProvider: UserIdentityProvider,
) : MviViewModel<PropertyIdentityState, PropertyIdentityEffect, PropertyIdentityAction>() {

    override val container = orbitContainer<PropertyIdentityState, PropertyIdentityEffect>(
        initialState = PropertyIdentityState(propertyId = propertyId)
    ) {
        observeData()
    }

    private fun observeData() = intent {
        val ownerId = userIdentityProvider.currentUserId()
        propertyRepository.getProperties(ownerId)
            .map { properties ->
                properties.firstOrNull { it.id == propertyId }
            }
            .catch { e ->
                reduce { state.copy(propertyState = UiState.Error(e.message ?: "Failed to load property")) }
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
