package com.gaatho.rent.features.property.presentation.list

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import kotlinx.coroutines.flow.catch
import org.orbitmvi.orbit.viewmodel.orbitContainer

class PropertyListViewModel(
    private val repository: PropertyRepository,
    private val userIdentityProvider: UserIdentityProvider,
    savedStateHandle: SavedStateHandle
) : MviViewModel<PropertyListState, PropertyListSideEffect, PropertyListAction>() {

    private val ownerId: String
        get() = userIdentityProvider.currentUserId()

    /**
     * Orbit container with full saved state support.
     *
     * Passing [savedStateHandle] + [serializer] means Orbit will:
     * 1. Restore the previous [PropertyListState] on process death recovery
     * 2. Persist any state mutation (`reduce {}`) immediately to the Bundle
     *
     * The `onCreate` lambda starts [observeProperties] — replaces `init {}`.
     */
    override val container = orbitContainer<PropertyListState, PropertyListSideEffect>(
        initialState = PropertyListState(),
        savedStateHandle = savedStateHandle,
        serializer = PropertyListState.serializer()
    ) {
        observeProperties()
    }

    /**
     * Single public entry point for all UI events.
     */
    override fun onAction(action: PropertyListAction) {
        when (action) {
            is PropertyListAction.OnPropertyClicked -> handlePropertyClick(action.propertyId)
            is PropertyListAction.OnAddPropertyClicked -> handleAddPropertyClick()
            is PropertyListAction.OnQuickActionClicked -> handleQuickAction(action.message)
            is PropertyListAction.Retry -> handleRetry()
        }
    }

    /* --- Private Intent Handlers --- */

    /**
     * Long-lived reactive observer. Subscribes once via `onCreate` and collects
     * for the ViewModel's lifetime.
     *
     * `catch {}` before `collect {}` handles any local database errors
     * before they reach the terminal operator and terminate the flow.
     */
    private fun observeProperties() = intent(registerIdling = false) {
        repository.getProperties(ownerId)
            .catch { e ->
                val msg = ErrorMessageExtractor.extract(e, "Could not load your properties. Please try again.")
                reduce { state.copy(propertiesState = UiState.Error(msg)) }
                postSideEffect(PropertyListSideEffect.ShowError(msg))
            }
            .collect { properties ->
                reduce {
                    state.copy(propertiesState = UiState.Success(properties))
                }
            }
    }

    /**
     * Resets to Loading state. The active [observeProperties] collector picks up
     * new data automatically — no re-subscription needed.
     */
    private fun handleRetry() = intent {
        reduce { state.copy(propertiesState = UiState.Loading) }
    }

    private fun handlePropertyClick(propertyId: String) = intent {
        postSideEffect(PropertyListSideEffect.NavigateToDetails(propertyId))
    }

    private fun handleAddPropertyClick() = intent {
        postSideEffect(PropertyListSideEffect.NavigateToAddProperty)
    }

    private fun handleQuickAction(message: String) = intent {
        postSideEffect(PropertyListSideEffect.ShowMessage(message))
    }
}
