package com.gaatho.rent.features.property.presentation.edit

import kotlinx.serialization.Serializable
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.Transient

@Serializable
data class PropertyUnitState(
    val id: String = "",
    @Transient val name: TextFieldValue = TextFieldValue(""),
    @Transient val monthlyRent: TextFieldValue = TextFieldValue("")
)

@Serializable
data class EditPropertyState(
    @Transient val name: TextFieldValue = TextFieldValue(),
    @Transient val streetAddress: TextFieldValue = TextFieldValue(),
    @Transient val city: TextFieldValue = TextFieldValue(),
    val propertyType: String = "APARTMENT",
    @Transient val totalUnits: TextFieldValue = TextFieldValue("1"),
    
    // Each unit now has its own state (name + rent string)
    val units: List<PropertyUnitState> = listOf(PropertyUnitState(name = TextFieldValue("Unit 1"))),
    
    // Base monthlyRent is removed as rent is now strictly per unit
    @Transient val wifiCharge: TextFieldValue = TextFieldValue(),
    @Transient val waterCharge: TextFieldValue = TextFieldValue(),
    @Transient val electricityCharge: TextFieldValue = TextFieldValue(),
    @Transient val wasteCharge: TextFieldValue = TextFieldValue(),
    @Transient val description: TextFieldValue = TextFieldValue(),
    @Transient val imageUrl: String? = null,
    @Transient val uploadedImageName: String? = null,
    @Transient val pendingImageBytes: ByteArray? = null,   // bytes staged for upload on Save
    @Transient val pendingImageName: String? = null,
    val billingCycle: String = "1st of the month",
    val selectedAmenities: Set<String> = setOf("Water", "Electricity"),

    // Loading / saving
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val isCompressingImage: Boolean = false,

    // Validation
    val nameError: String? = null,
    val addressError: String? = null,

    // Persistence
    val originalCreatedAt: String? = null
) {
    val unitsCount: Int get() = units.size
    val allAmenities: List<String> get() = (listOf("Water", "Electricity", "WiFi", "Parking", "Security", "Waste") + selectedAmenities).distinct()
    val availablePropertyTypes: List<String> get() = listOf("HOUSE", "APARTMENT", "FLAT", "SHOP", "BUILDING")
}

sealed interface EditPropertySideEffect {
    data object NavigateBack : EditPropertySideEffect
    data object NavigateToPropertyList : EditPropertySideEffect
    data class ShowSnackbar(val message: String) : EditPropertySideEffect
}

sealed interface EditPropertyAction {
    data class OnNameChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnStreetAddressChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnCityChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnTypeChanged(val type: String) : EditPropertyAction
    data class OnTotalUnitsChanged(val value: TextFieldValue) : EditPropertyAction
    
    // Updated unit actions
    data class OnUnitNameChanged(val index: Int, val name: TextFieldValue) : EditPropertyAction
    data class OnUnitRentChanged(val index: Int, val rent: TextFieldValue) : EditPropertyAction
    

    data class OnWifiChargeChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnWaterChargeChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnElectricityChargeChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnWasteChargeChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnDescriptionChanged(val value: TextFieldValue) : EditPropertyAction
    data class OnBillingCycleChanged(val cycle: String) : EditPropertyAction
    data class OnAmenityToggled(val amenity: String) : EditPropertyAction
    data class OnAddCustomAmenity(val amenity: String) : EditPropertyAction
    
    data class OnImagePicked(val name: String, val bytes: ByteArray) : EditPropertyAction
    data object OnSaveClicked : EditPropertyAction
    data object OnSuccessDialogDismissed : EditPropertyAction
    data object OnBackClicked : EditPropertyAction
    data object OnDeleteClicked : EditPropertyAction
    data object OnDeleteDismissed : EditPropertyAction
    data object OnDeleteConfirmed : EditPropertyAction
}
