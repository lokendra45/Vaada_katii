package com.gaatho.rent.features.tenant.presentation.edit

import androidx.compose.ui.text.input.TextFieldValue

sealed class EditTenantAction {
    data class OnNameChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnPhoneChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnEmailChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnRentChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnUnitNumberChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnMoveInDateChanged(val date: String) : EditTenantAction()
    data class OnLeaseDurationSelected(val duration: String) : EditTenantAction()
    data class OnSecurityDepositChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnPropertySelected(val propertyId: String) : EditTenantAction()
    data class OnStatusSelected(val status: String) : EditTenantAction()

    object OnSaveClicked : EditTenantAction()
    object OnSuccessDialogDismissed : EditTenantAction()
    object OnBackClicked : EditTenantAction()
    object OnDeleteClicked : EditTenantAction()
    object OnDeleteDismissed : EditTenantAction()
    object OnDeleteConfirmed : EditTenantAction()
}