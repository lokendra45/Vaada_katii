package com.gaatho.rent.features.property.presentation.list

import androidx.compose.runtime.Immutable
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.domain.model.Property
import kotlinx.serialization.Serializable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Represents the immutable UI state for the Property List screen.
 *
 * ## Annotations
 * - `@Immutable` — Tells the Compose compiler all properties are stable, enabling
 *   it to skip recompositions when the state reference hasn't changed.
 * - `@Serializable` — Required by Orbit's saved state mechanism (KMP-compatible).
 *   Allows the container to persist and restore state across process death.
 *   See: `orbitContainer(savedStateHandle, serializer = PropertyListState.serializer())`
 *
 * @property propertiesState The loading/success/error state of the property list.
 */
@Serializable
@Immutable
data class PropertyDisplayModel(
    val id: String,
    val name: String,
    val address: String,
    val imageUrl: String?,
    val totalUnits: Int,
    val occUnits: Int,
    val statusBadge: String,
    val isVacant: Boolean,
    val pendingText: String,
    val isPending: Boolean
)

@Serializable
@Immutable
data class PropertyListState(
    // We map raw Property to PropertyDisplayModel so the UI does zero logic
    val propertiesState: UiState<ImmutableList<PropertyDisplayModel>> = UiState.Idle,
    val searchQuery: String = "",
    val selectedLocation: String = "All properties",
    // Pre-computed by ViewModel on Dispatchers.Default — never on the UI thread
    val filteredProperties: ImmutableList<PropertyDisplayModel> = persistentListOf()
) {
    val allProperties: ImmutableList<PropertyDisplayModel>
        get() = (propertiesState as? UiState.Success)?.data ?: persistentListOf()
}

/**
 * One-time side effects for the Property List screen.
 *
 * These are backed by Orbit's side-effect Channel (capacity = UNLIMITED) which
 * guarantees delivery exactly once. They are never stored as state — the UI
 * consumes them and they disappear.
 *
 * Note: For Snackbar messages, Google recommends the "consume from state" pattern
 * (`List<UserMessage>` in [PropertyListState]). Orbit's Channel approach is an
 * acceptable pragmatic trade-off and is widely used in the Orbit community.
 */
sealed interface PropertyListSideEffect {

    /** Navigate to the detail screen for the given property. */
    data class NavigateToDetails(val propertyId: String) : PropertyListSideEffect

    /** Navigate to the Add Property screen. */
    data object NavigateToAddProperty : PropertyListSideEffect

    /** Show a Snackbar with the given error message. */
    data class ShowError(val message: String) : PropertyListSideEffect

    /** Show an informational Snackbar message. */
    data class ShowMessage(val message: String) : PropertyListSideEffect
}

/**
 * All possible user interactions on the Property List screen.
 *
 * This is the ONLY mechanism the UI uses to communicate with the ViewModel.
 * No public functions on the ViewModel are called directly.
 *
 * Note: There is intentionally NO `LoadProperties` action. Initial data loading
 * is triggered internally by the ViewModel's `orbitContainer { onCreate }` block.
 * Exposing a public `LoadProperties` action would be dead code since the ViewModel
 * loads data automatically — the UI never needs to trigger this manually.
 */
sealed interface PropertyListAction {

    /**
     * A property card was clicked.
     * @property propertyId ID of the tapped property.
     */
    data class OnPropertyClicked(val propertyId: String) : PropertyListAction

    /** The "Add Property" button was tapped. */
    data object OnAddPropertyClicked : PropertyListAction

    /** Search query changed inside AppSearchBar. */
    data class OnSearchQueryChanged(val query: String) : PropertyListAction

    /** A location filter pill was clicked. */
    data class OnLocationFilterSelected(val location: String) : PropertyListAction

    /** A quick action pill was clicked. */
    data class OnQuickActionClicked(val message: String) : PropertyListAction

    /** The user tapped "Retry" after a failed data load. */
    data object Retry : PropertyListAction
}
