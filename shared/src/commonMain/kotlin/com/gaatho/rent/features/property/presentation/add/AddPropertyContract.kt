package com.gaatho.rent.features.property.presentation.add

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Immutable UI state for the Add Property screen.
 * Persisted across process death via Orbit MVI's SavedStateHandle.
 */
@Serializable
@Immutable
data class AddPropertyState(
    val name: String = "",
    val address: String = "",
    val propertyType: String = "HOUSE", // Hardcoded for now as per plan
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val addressError: String? = null
)

/**
 * One-time side effects for the Add Property screen.
 */
sealed interface AddPropertySideEffect {
    /** Navigates back to the previous screen. */
    data object NavigateBack : AddPropertySideEffect

    /** Shows a Snackbar with a message (e.g. "No internet connection"). */
    data class ShowSnackbar(val message: String) : AddPropertySideEffect
}

/**
 * User actions triggered from the Add Property UI.
 */
sealed interface AddPropertyAction {
    data class OnNameChanged(val name: String) : AddPropertyAction
    data class OnAddressChanged(val address: String) : AddPropertyAction
    data object OnSaveClicked : AddPropertyAction
}
