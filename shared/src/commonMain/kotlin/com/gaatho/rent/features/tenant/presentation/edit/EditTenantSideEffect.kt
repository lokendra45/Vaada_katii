package com.gaatho.rent.features.tenant.presentation.edit


sealed interface EditTenantSideEffect {
    object NavigateBack : EditTenantSideEffect
    object NavigateToTenantList : EditTenantSideEffect
    data class ShowSnackbar(val message: String) : EditTenantSideEffect
}
