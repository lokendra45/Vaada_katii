package com.gaatho.rent.features.property.presentation.edit

import kotlinx.serialization.Serializable

@Serializable
data class EditPropertyState(
    // Form fields (same as AddPropertyState)
    val name: String = "",
    val streetAddress: String = "",
    val city: String = "",
    val zipCode: String = "",
    val propertyType: String = "HOUSE",
    val totalUnits: String = "1",
    val billingCycle: String = "1st of the month",
    val selectedAmenities: Set<String> = setOf("Water", "Electricity"),

    // Loading / saving
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    // Validation
    val nameError: String? = null,
    val addressError: String? = null,
)

sealed interface EditPropertySideEffect {
    data object NavigateBack : EditPropertySideEffect
    data class ShowSnackbar(val message: String) : EditPropertySideEffect
}

sealed interface EditPropertyAction {
    data class OnNameChanged(val name: String) : EditPropertyAction
    data class OnStreetAddressChanged(val address: String) : EditPropertyAction
    data class OnCityChanged(val city: String) : EditPropertyAction
    data class OnZipCodeChanged(val zip: String) : EditPropertyAction
    data class OnTypeChanged(val type: String) : EditPropertyAction
    data class OnTotalUnitsChanged(val units: String) : EditPropertyAction
    data class OnBillingCycleChanged(val cycle: String) : EditPropertyAction
    data class OnAmenityToggled(val amenity: String) : EditPropertyAction
    data object OnSaveClicked : EditPropertyAction
    data object OnBackClicked : EditPropertyAction
}
