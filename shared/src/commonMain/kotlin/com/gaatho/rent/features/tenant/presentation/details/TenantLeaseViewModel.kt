package com.gaatho.rent.features.tenant.presentation.details

import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import kotlinx.coroutines.flow.collectLatest
import org.orbitmvi.orbit.viewmodel.orbitContainer
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.utils.CurrencyUtil

class TenantLeaseViewModel(
    private val tenantId: String,
    private val tenantRepository: TenantRepository
) : MviViewModel<TenantLeaseState, TenantLeaseEffect, TenantLeaseAction>() {

    override val container = orbitContainer<TenantLeaseState, TenantLeaseEffect>(
        initialState = TenantLeaseState(tenantId = tenantId)
    ) {
        loadLease()
    }

    private fun loadLease() = intent(registerIdling = false) {
        tenantRepository.getTenantById(tenantId).collectLatest { tenant ->
            if (tenant == null) {
                reduce { state.copy(leaseState = UiState.Error("Lease details not found")) }
                return@collectLatest
            }
            
            val lease = TenantLeaseDisplayModel(
                monthlyRent = CurrencyUtil.formatNprLabel(tenant.rentAmount),
                status = tenant.status,
                isActive = tenant.status == "Active",
                startDate = DateTimeUtil.formatReadableDate(tenant.createdAt),
                endDate = "Ongoing"
            )
            
            reduce { state.copy(leaseState = UiState.Success(lease)) }
        }
    }

    override fun onAction(action: TenantLeaseAction) {
        intent {
            when (action) {
                is TenantLeaseAction.OnMaintenanceClicked -> {
                    postSideEffect(TenantLeaseEffect.ShowToast("Maintenance clicked"))
                }
            }
        }
    }
}
