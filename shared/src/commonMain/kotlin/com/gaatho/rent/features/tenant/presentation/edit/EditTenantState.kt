package com.gaatho.rent.features.tenant.presentation.edit

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.Transient

data class EditTenantState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showSuccessDialog: Boolean = false,
    
    @Transient val name: TextFieldValue = TextFieldValue(),
    val nameError: String? = null,
    @Transient val phone: TextFieldValue = TextFieldValue(),
    @Transient val email: TextFieldValue = TextFieldValue(),
    @Transient val rentAmount: TextFieldValue = TextFieldValue(),
    val rentError: String? = null,
    @Transient val roomNumber: TextFieldValue = TextFieldValue(),
    val status: String = "Active",
    val propertyId: String? = null,
    val propertyOptions: List<PropertyOption> = emptyList()
)

data class PropertyOption(
    val id: String,
    val name: String
)
