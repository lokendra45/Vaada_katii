package com.gaatho.rent.features.tenant.presentation.edit

sealed interface EditTenantAction {
    data class OnNameChanged(val name: String) : EditTenantAction
    data class OnPhoneChanged(val phone: String) : EditTenantAction
    data class OnEmailChanged(val email: String) : EditTenantAction
    data class OnRentChanged(val rent: String) : EditTenantAction
    data class OnRoomNumberChanged(val roomNumber: String) : EditTenantAction
    data class OnPropertySelected(val propertyId: String) : EditTenantAction
    data class OnStatusSelected(val status: String) : EditTenantAction
    
    object OnSaveClicked : EditTenantAction
    object OnBackClicked : EditTenantAction
}
