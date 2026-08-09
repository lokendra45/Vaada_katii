package com.gaatho.rent.features.property.presentation.add

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Immutable UI state for the Add Property screen.
 * Persisted across process death via Orbit MVI's SavedStateHandle.
 */
@Serializable
@Immutable
data class AddPropertyState(
    @Transient val name: TextFieldValue = TextFieldValue(),
    @Transient val streetAddress: TextFieldValue = TextFieldValue(),
    @Transient val city: TextFieldValue = TextFieldValue(),
    val propertyType: String = "HOUSE",
    @Transient val totalUnits: TextFieldValue = TextFieldValue("1"),
    val billingCycle: String = "1st of the month",
    val selectedAmenities: Set<String> = setOf("Water", "Electricity"),
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val addressError: String? = null,
    val cityError: String? = null,
    val unitsError: String? = null,
    val imageBytes: ByteArray? = null
)

/**
 * One-time side effects for the Add Property screen.
 */
sealed interface AddPropertySideEffect {
    /** Navigates back to the previous screen. */
    data object NavigateBack : AddPropertySideEffect
    data object ShowSuccessDialog : AddPropertySideEffect

    /** Shows a Snackbar with a message (e.g. "No internet connection"). */
    data class ShowSnackbar(val message: String) : AddPropertySideEffect
}

/**
 * User actions triggered from the Add Property UI.
 */
sealed interface AddPropertyAction {
    data class OnNameChanged(val value: TextFieldValue) : AddPropertyAction
    data class OnStreetAddressChanged(val value: TextFieldValue) : AddPropertyAction
    data class OnCityChanged(val value: TextFieldValue) : AddPropertyAction
    data class OnTypeChanged(val type: String) : AddPropertyAction
    data class OnTotalUnitsChanged(val value: TextFieldValue) : AddPropertyAction
    data class OnBillingCycleChanged(val cycle: String) : AddPropertyAction
    data class OnAmenityToggled(val amenity: String) : AddPropertyAction
    data class OnImagePicked(val bytes: ByteArray?) : AddPropertyAction
    data object OnSaveClicked : AddPropertyAction
}
