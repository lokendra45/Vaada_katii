package com.gaatho.rent.features.tenant.presentation.details

import com.gaatho.rent.core.ui.UiState
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TenantLeaseState(
    val tenantId: String = "",
    @Transient
    val leaseState: UiState<TenantLeaseDisplayModel> = UiState.Idle
)

sealed interface TenantLeaseAction {
    data object OnMaintenanceClicked : TenantLeaseAction
}

sealed interface TenantLeaseEffect {
    data class ShowToast(val message: String) : TenantLeaseEffect
}
