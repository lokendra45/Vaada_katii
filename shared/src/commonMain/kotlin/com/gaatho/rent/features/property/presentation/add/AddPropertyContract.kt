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
    val streetAddress: String = "",
    val city: String = "",
    val zipCode: String = "",
    val propertyType: String = "HOUSE",
    val totalUnits: String = "1",
    val billingCycle: String = "1st of the month",
    val selectedAmenities: Set<String> = setOf("Water", "Electricity"),
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
    data class OnStreetAddressChanged(val address: String) : AddPropertyAction
    data class OnCityChanged(val city: String) : AddPropertyAction
    data class OnZipCodeChanged(val zip: String) : AddPropertyAction
    data class OnTypeChanged(val type: String) : AddPropertyAction
    data class OnTotalUnitsChanged(val units: String) : AddPropertyAction
    data class OnBillingCycleChanged(val cycle: String) : AddPropertyAction
    data class OnAmenityToggled(val amenity: String) : AddPropertyAction
    data object OnSaveClicked : AddPropertyAction
}
