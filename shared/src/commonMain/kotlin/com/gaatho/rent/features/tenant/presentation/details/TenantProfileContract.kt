package com.gaatho.rent.features.tenant.presentation.details

import com.gaatho.rent.core.ui.UiState
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TenantProfileState(
    val tenantId: String = "",
    @Transient
    val profileState: UiState<TenantProfileDisplayModel> = UiState.Idle,
    val showDeleteConfirm: Boolean = false,
    val isDeleting: Boolean = false
)

sealed interface TenantProfileAction {
    data object OnEditClicked : TenantProfileAction
    data object OnCallClicked : TenantProfileAction
    data object OnMessageClicked : TenantProfileAction
    data object OnEmailClicked : TenantProfileAction
    data object OnDeleteClicked : TenantProfileAction
    data object OnDeleteDismissed : TenantProfileAction
    data object OnDeleteConfirmed : TenantProfileAction
}

sealed interface TenantProfileEffect {
    data class NavigateToEdit(val tenantId: String) : TenantProfileEffect
    data class OpenPhoneApp(val phone: String) : TenantProfileEffect
    data class OpenSmsApp(val phone: String) : TenantProfileEffect
    data class OpenEmailApp(val email: String) : TenantProfileEffect
    data class ShowToast(val message: String) : TenantProfileEffect
    data class ShowError(val message: String) : TenantProfileEffect
    data object NavigateBack : TenantProfileEffect
}
