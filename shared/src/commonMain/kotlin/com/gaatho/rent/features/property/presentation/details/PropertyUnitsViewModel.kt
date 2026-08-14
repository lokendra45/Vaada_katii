package com.gaatho.rent.features.property.presentation.details

import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.viewmodel.orbitContainer

class PropertyUnitsViewModel(
    private val propertyId: String,
    private val tenantRepository: TenantRepository,
    private val userIdentityProvider: UserIdentityProvider,
) : MviViewModel<PropertyUnitsState, PropertyUnitsEffect, PropertyUnitsAction>() {

    override val container = orbitContainer<PropertyUnitsState, PropertyUnitsEffect>(
        initialState = PropertyUnitsState(propertyId = propertyId)
    ) {
        observeData()
    }

    private fun observeData() = intent {
        val ownerId = userIdentityProvider.currentUserId()
        tenantRepository.getTenants(ownerId)
            .map { tenants -> tenants.filter { it.propertyId == propertyId } }
            .catch { e ->
                reduce { state.copy(unitsState = UiState.Error(e.message ?: "Failed to load units")) }
            }
            .collect { tenants ->
                val units = buildUnitList(tenants)
                reduce {
                    state.copy(unitsState = UiState.Success(units.toImmutableList()))
                }
            }
    }

    private fun buildUnitList(tenants: List<Tenant>): List<UnitDisplayModel> {
        if (tenants.isEmpty()) return emptyList()
        return tenants.map { tenant ->
            val status = when {
                tenant.status.equals("Overdue", ignoreCase = true) -> UnitPaymentStatus.OVERDUE
                tenant.status.equals("Active", ignoreCase = true) -> UnitPaymentStatus.PAID
                else -> UnitPaymentStatus.VACANT
            }
            UnitDisplayModel(
                unitNumber = tenant.roomNumber ?: "—",
                tenantName = if (status == UnitPaymentStatus.VACANT) null else tenant.name,
                rentPerMonth = tenant.rentAmount,
                paymentStatus = status,
            )
        }
    }

    override fun onAction(action: PropertyUnitsAction) {}
}
