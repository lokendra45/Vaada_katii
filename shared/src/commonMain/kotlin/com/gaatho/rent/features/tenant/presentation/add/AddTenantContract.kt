package com.gaatho.rent.features.tenant.presentation.add

import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import androidx.compose.ui.text.input.TextFieldValue

@Serializable
data class AddTenantState(
    @Transient val fullName: TextFieldValue = TextFieldValue(),
    @Transient val phone: TextFieldValue = TextFieldValue(),
    @Transient val email: TextFieldValue = TextFieldValue(),
    @Transient val address: TextFieldValue = TextFieldValue(),
    @Transient val occupation: TextFieldValue = TextFieldValue(),
    @Transient val roomNumber: TextFieldValue = TextFieldValue(),
    val startDate: String = "",
    val endDate: String = "",
    @Transient val deposit: TextFieldValue = TextFieldValue(),
    @Transient val rentAmount: TextFieldValue = TextFieldValue(),
    
    @Transient
    val propertiesState: UiState<ImmutableList<PropertySelectionModel>> = UiState.Loading,
    val selectedPropertyId: String? = null,
    
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val showDatePicker: Boolean = false,
    val isSelectingStartDate: Boolean = true
) {
    val isFormValid: Boolean
        get() = fullName.text.isNotBlank() && 
                selectedPropertyId != null && 
                rentAmount.text.isNotBlank() && 
                deposit.text.isNotBlank()

    val canSubmit: Boolean
        get() = !isSaving && isFormValid
}

@Serializable
data class PropertySelectionModel(
    val id: String,
    val name: String
)

sealed class AddTenantAction {
    data class OnFullNameChanged(val value: TextFieldValue) : AddTenantAction()
    data class OnPhoneChanged(val value: TextFieldValue) : AddTenantAction()
    data class OnEmailChanged(val value: TextFieldValue) : AddTenantAction()
    data class OnAddressChanged(val value: TextFieldValue) : AddTenantAction()
    data class OnOccupationChanged(val value: TextFieldValue) : AddTenantAction()
    data class OnRoomNumberChanged(val value: TextFieldValue) : AddTenantAction()
    data class OnDepositChanged(val value: TextFieldValue) : AddTenantAction()
    data class OnRentAmountChanged(val value: TextFieldValue) : AddTenantAction()
    
    data class OnPropertySelected(val id: String) : AddTenantAction()
    
    data class OnDateFieldClicked(val isStartDate: Boolean) : AddTenantAction()
    data class OnDateSelected(val date: String) : AddTenantAction()
    data object OnDatePickerDismissed : AddTenantAction()
    
    data object OnSaveClicked : AddTenantAction()
}

sealed class AddTenantSideEffect {
    data object NavigateBack : AddTenantSideEffect()
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : AddTenantSideEffect()
}
