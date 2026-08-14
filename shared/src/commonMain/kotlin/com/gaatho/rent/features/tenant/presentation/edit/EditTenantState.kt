package com.gaatho.rent.features.tenant.presentation.edit

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.Transient

data class EditTenantState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,

    @Transient val name: TextFieldValue = TextFieldValue(),
    val nameError: String? = null,
    @Transient val phone: TextFieldValue = TextFieldValue(),
    @Transient val email: TextFieldValue = TextFieldValue(),
    @Transient val rentAmount: TextFieldValue = TextFieldValue(),
    val rentError: String? = null,
    @Transient val unitNumber: TextFieldValue = TextFieldValue(),
    @Transient val moveInDate: String = "",
    val leaseDuration: String = "1 Year",
    @Transient val securityDeposit: TextFieldValue = TextFieldValue(),
    val status: String = "Active",
    val propertyId: String? = null,
    val propertyOptions: List<PropertyOption> = emptyList(),
    @Transient val uploadedDocumentName: String? = null
)

data class PropertyOption(
    val id: String,
    val name: String
)