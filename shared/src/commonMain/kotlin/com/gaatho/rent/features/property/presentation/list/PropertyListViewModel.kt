package com.gaatho.rent.features.property.presentation.list

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import kotlinx.coroutines.flow.catch
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.orbitmvi.orbit.viewmodel.orbitContainer
import io.github.jan.supabase.auth.auth

/**
 * ViewModel for the Property List screen.
 *
 * Extends [MviViewModel] which correctly implements
 * `OrbitContainerHost<STATE, STATE, SIDE_EFFECT>` per the official Orbit v12 docs.
 *
 * ## Saved State (Process Death Recovery)
 * [SavedStateHandle] is passed to [orbitContainer] alongside the state [kotlinx.serialization.KSerializer].
 * When the OS kills the app process (low memory), Orbit serializes the state into
 * the [SavedStateHandle] (backed by `Bundle`). On the next launch, the state is
 * restored exactly, so the user sees their last-known property list instead of a
 * blank loading spinner.
 *
 * This works cross-platform:
 * - **Android**: `SavedStateHandle` is provided automatically by the Jetpack
 *   ViewModel factory. Koin's `viewModel { }` block wires this via `get()`.
 * - **iOS**: The JetBrains multiplatform lifecycle library provides `SavedStateHandle`
 *   for KMP. On iOS it uses NSUserDefaults under the hood.
 *
 * ## Orbit Patterns Used
 *
 * ### `orbitContainer { onCreate }` instead of `init {}`
 * The trailing lambda runs inside the container's own coroutine scope, after the
 * container is fully initialized. No init-order bugs possible.
 *
 * ### `intent(registerIdling = false) { collect {} }` for long-lived flows
 * The Store5 + SQLDelight flow never terminates — it emits whenever local data changes.
 * A single `intent` block suspends on `collect` for the ViewModel lifetime:
 * - No nested `intent {}` inside `onEach {}`
 * - No manual `Job` tracking
 * - `registerIdling = false` prevents Espresso from waiting forever
 *
 * ### No `LoadProperties` action
 * Loading starts via `onCreate`. There is nothing for the UI to trigger explicitly.
 *
 * TODO: Replace [ownerId] with `sessionManager.requireCurrentUserId()` once the
 *       Auth module is built.
 *
 * @param repository Provides the reactive property stream (Store5 + SQLDelight).
 * @param savedStateHandle Jetpack/KMP handle for persisting state across process death.
 */
class PropertyListViewModel(
    private val repository: PropertyRepository,
    private val supabase: io.github.jan.supabase.SupabaseClient,
    savedStateHandle: SavedStateHandle
) : MviViewModel<PropertyListState, PropertyListSideEffect, PropertyListAction>() {

    private val ownerId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: ""

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
     * `catch {}` before `collect {}` handles upstream (Supabase fetcher) errors
     * before they reach the terminal operator and terminate the flow.
     */
    private fun observeProperties() = intent(registerIdling = false) {
        repository.getProperties(ownerId)
            .catch { e ->
                val msg = ErrorMessageExtractor.extract(e, "Could not load your properties. Please try again.")
                reduce { state.copy(propertiesState = UiState.Error(msg)) }
                postSideEffect(PropertyListSideEffect.ShowError(msg))
            }
            .collect { response ->
                when (response) {
                    is StoreReadResponse.Loading -> reduce {
                        state.copy(propertiesState = UiState.Loading)
                    }
                    is StoreReadResponse.Data -> reduce {
                        state.copy(propertiesState = UiState.Success(response.value))
                    }
                    is StoreReadResponse.Error.Exception -> {
                        val msg = ErrorMessageExtractor.extract(response.error, "Could not sync properties from server.")
                        reduce { state.copy(propertiesState = UiState.Error(msg)) }
                        postSideEffect(PropertyListSideEffect.ShowError(msg))
                    }
                    is StoreReadResponse.Error.Message -> {
                        val msg = ErrorMessageExtractor.extractFromString(response.message, "Could not sync properties from server.")
                        reduce { state.copy(propertiesState = UiState.Error(msg)) }
                        postSideEffect(PropertyListSideEffect.ShowError(msg))
                    }
                    else -> Unit
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
