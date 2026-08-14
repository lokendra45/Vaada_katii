package com.gaatho.rent.features.property.presentation.edit

import kotlinx.serialization.Serializable
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.Transient

@Serializable
data class EditPropertyState(
    // Form fields (same as AddPropertyState)
    @Transient val name: TextFieldValue = TextFieldValue(),
    @Transient val streetAddress: TextFieldValue = TextFieldValue(),
    @Transient val city: TextFieldValue = TextFieldValue(),
    val propertyType: String = "APARTMENT",
    @Transient val totalUnits: TextFieldValue = TextFieldValue("1"),
    @Transient val monthlyRent: TextFieldValue = TextFieldValue(),
    @Transient val description: TextFieldValue = TextFieldValue(),
    val billingCycle: String = "1st of the month",
    val selectedAmenities: Set<String> = setOf("Water", "Electricity"),

    // Loading / saving
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,

    // Validation
    val nameError: String? = null,
    val addressError: String? = null,
)

sealed interface EditPropertySideEffect {
    data object NavigateBack : EditPropertySideEffect
    data class ShowSnackbar(val message: String) : EditPropertySideEffect
}

sealed interface EditPropertyAction {
    data class OnNameChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnStreetAddressChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnCityChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnTypeChanged(val type: String) : EditPropertyAction
    data class OnTotalUnitsChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnMonthlyRentChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnDescriptionChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnBillingCycleChanged(val cycle: String) : EditPropertyAction
    data class OnAmenityToggled(val amenity: String) : EditPropertyAction
    data object OnSaveClicked : EditPropertyAction
    data object OnSuccessDialogDismissed : EditPropertyAction
    data object OnBackClicked : EditPropertyAction
    data object OnDeleteClicked : EditPropertyAction
    data object OnDeleteDismissed : EditPropertyAction
    data object OnDeleteConfirmed : EditPropertyAction
}
