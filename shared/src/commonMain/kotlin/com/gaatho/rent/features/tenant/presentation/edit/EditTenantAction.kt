package com.gaatho.rent.features.tenant.presentation.edit

import androidx.compose.ui.text.input.TextFieldValue

sealed class EditTenantAction {
    data class OnNameChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnPhoneChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnEmailChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnRentChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnRoomNumberChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnPropertySelected(val propertyId: String) : EditTenantAction()
    data class OnStatusSelected(val status: String) : EditTenantAction()
    
    object OnSaveClicked : EditTenantAction()
    object OnSuccessDialogDismissed : EditTenantAction()
    object OnBackClicked : EditTenantAction()
}
