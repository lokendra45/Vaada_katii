package com.gaatho.rent.features.tenant.presentation.details

import com.gaatho.rent.core.mvi.MviViewModel
import org.orbitmvi.orbit.viewmodel.orbitContainer

class TenantDetailsViewModel(
    private val tenantId: String
) : MviViewModel<TenantDetailsState, TenantDetailsEffect, TenantDetailsAction>() {

    override val container = orbitContainer<TenantDetailsState, TenantDetailsEffect>(
        initialState = TenantDetailsState(tenantId = tenantId)
    )

    override fun onAction(action: TenantDetailsAction) {
        intent {
            when (action) {
                is TenantDetailsAction.OnBackClicked -> {
                    postSideEffect(TenantDetailsEffect.NavigateBack)
                }
            }
        }
    }
}
