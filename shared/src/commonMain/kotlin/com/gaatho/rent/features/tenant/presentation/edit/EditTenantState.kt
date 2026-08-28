package com.gaatho.rent.features.tenant.presentation.edit

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.Transient

data class EditTenantState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,

    @Transient     val name: TextFieldValue = TextFieldValue(),
    val nameError: String? = null,
    @Transient val phone: TextFieldValue = TextFieldValue(),
    val phoneError: String? = null,
    @Transient val email: TextFieldValue = TextFieldValue(),
    val emailError: String? = null,
    @Transient val rentAmount: TextFieldValue = TextFieldValue(),
    val rentError: String? = null,
    @Transient val unitNumber: TextFieldValue = TextFieldValue(),
    @Transient val moveInDate: String = "",
    val leaseDuration: String = "1 Year",
    @Transient val securityDeposit: TextFieldValue = TextFieldValue(),
    val status: String = "Active",
    val propertyId: String? = null,
    val propertyOptions: List<PropertyOption> = emptyList(),
    @Transient val uploadedDocumentName: String? = null,
    val profileImageUrl: String? = null,
    val documentType: String = "Citizenship",
    val documentUrl: String? = null,
    // Pending bytes — stored locally until user taps Save
    @Transient val pendingProfileBytes: ByteArray? = null,
    @Transient val pendingProfileName: String? = null,
    @Transient val pendingDocBytes: ByteArray? = null,
    @Transient val pendingDocName: String? = null,
    val hasWifi: Boolean = false,
    val hasWater: Boolean = false,
    val hasElectricity: Boolean = false,
    val hasWaste: Boolean = false,
    val paymentDueDate: String = "",
    val originalCreatedAt: String? = null
) {
    val propertyNames: List<String> get() = propertyOptions.map { it.name }
    val selectedPropertyName: String? get() = propertyOptions.find { it.id == propertyId }?.name
    val selectedProperty: PropertyOption? get() = propertyOptions.find { it.id == propertyId }
    val unitOptions: List<String> get() = selectedProperty?.units?.map { it.name } ?: emptyList()
    
    val wifiLabel: String get() = if (selectedProperty != null && selectedProperty!!.wifiCharge > 0) "WiFi (NPR ${selectedProperty!!.wifiCharge})" else "WiFi"
    val waterLabel: String get() = if (selectedProperty != null && selectedProperty!!.waterCharge > 0) "Water (NPR ${selectedProperty!!.waterCharge})" else "Water"
    val electricityLabel: String get() = if (selectedProperty != null && selectedProperty!!.electricityCharge > 0) "Electricity (NPR ${selectedProperty!!.electricityCharge})" else "Electricity"
    val wasteLabel: String get() = if (selectedProperty != null && selectedProperty!!.wasteCharge > 0) "Waste (NPR ${selectedProperty!!.wasteCharge})" else "Waste"
    
    val availableLeaseDurations: List<String> get() = listOf("1 Year", "2 Years", "3 Years", "5 Years")
    val availableDocumentTypes: List<String> get() = listOf("Citizenship", "Passport", "Driving License", "National ID")
}

data class PropertyUnitOption(
    val id: String,
    val name: String,
    val monthlyRent: Long
)

data class PropertyOption(
    val id: String,
    val name: String,
    val monthlyRent: Long = 0L,
    val wifiCharge: Long = 0L,
    val waterCharge: Long = 0L,
    val electricityCharge: Long = 0L,
    val wasteCharge: Long = 0L,
    val units: List<PropertyUnitOption> = emptyList()
)
