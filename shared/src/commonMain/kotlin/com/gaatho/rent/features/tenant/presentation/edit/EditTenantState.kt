package com.gaatho.rent.features.tenant.presentation.edit

data class EditTenantState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val name: String = "",
    val nameError: String? = null,
    val phone: String = "",
    val email: String = "",
    val rentAmount: String = "",
    val rentError: String? = null,
    val roomNumber: String = "",
    val status: String = "Active",
    val propertyId: String? = null,
    val propertyOptions: List<PropertyOption> = emptyList()
)

data class PropertyOption(
    val id: String,
    val name: String
)
