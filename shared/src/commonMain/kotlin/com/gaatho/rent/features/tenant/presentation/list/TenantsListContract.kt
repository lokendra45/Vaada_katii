package com.gaatho.rent.features.tenant.presentation.list

import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.serialization.Serializable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Transient

@Serializable
data class TenantDisplayModel(
    val id: String,
    val name: String,
    val initials: String,
    val subtitle: String,
    val status: String,
    val isActive: Boolean,
    val avatarBgColorHex: Long,
    val avatarTextColorHex: Long,
    val propertyName: String?,
    val propertyId: String?,
    val roomNumber: String?,
    val email: String?,
    val phone: String?,
    val rentAmount: Long = 0L
)

/**
 * Represents the immutable UI state for the Tenants List screen.
 *
 * [filteredTenants] is a pre-computed field updated by the ViewModel on
 * Dispatchers.Default — never computed on the Compose Main thread.
 */
@Serializable
data class TenantsListState(
    val selectedStatus: String = "All statuses",
    val selectedProperty: String = "All properties",
    @Transient
    val propertiesState: UiState<ImmutableList<Property>> = UiState.Idle,
    val debouncedQuery: String = ""
)

sealed interface TenantsListSideEffect {
    data class NavigateToTenantDetails(val tenantId: String) : TenantsListSideEffect
    data class NavigateToAddPayment(val tenantId: String, val propertyId: String?) : TenantsListSideEffect
    data class ShowError(val message: String) : TenantsListSideEffect
    data class ShowMessage(val message: String) : TenantsListSideEffect
    data class ShowArchivedPrompt(
        val tenantId: String, 
        val tenantName: String,
        val profileInfo: String,
        val rentInfo: String
    ) : TenantsListSideEffect
}

sealed interface TenantsListAction {
    data class OnStatusFilterChanged(val status: String) : TenantsListAction
    data class OnPropertyFilterChanged(val propertyName: String) : TenantsListAction
    data class OnTenantClicked(val tenantId: String) : TenantsListAction
    data class OnRecordPaymentClicked(val tenantId: String, val propertyId: String?) : TenantsListAction
    data class OnArchivedTenantBackupCompleted(val tenantId: String) : TenantsListAction
    data object OnRetry : TenantsListAction
}
