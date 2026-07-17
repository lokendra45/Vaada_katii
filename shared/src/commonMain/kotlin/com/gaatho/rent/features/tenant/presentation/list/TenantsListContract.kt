package com.gaatho.rent.features.tenant.presentation.list

import androidx.compose.runtime.Immutable
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.serialization.Serializable

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
    val roomNumber: String?,
    val email: String?,
    val phone: String?
)

/**
 * Represents the immutable UI state for the Tenants List screen.
 */
@Serializable
@Immutable
data class TenantsListState(
    val tenantsState: UiState<List<TenantDisplayModel>> = UiState.Idle,
    val propertiesState: UiState<List<Property>> = UiState.Idle,
    val searchQuery: String = "",
    val selectedStatus: String = "All statuses",
    val selectedProperty: String = "All properties"
) {
    /**
     * Helper to compute the active list of tenants regardless of state.
     */
    val allTenants: List<TenantDisplayModel>
        get() = (tenantsState as? UiState.Success)?.data ?: emptyList()

    /**
     * Computes the filtered list based on current search query and selected dropdown filters.
     */
    val filteredTenants: List<TenantDisplayModel>
        get() {
            val raw = allTenants
            return raw.filter { tenant ->
                // 1. Search Query filter (matches name, email, phone, or property name)
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    val q = searchQuery.trim().lowercase()
                    tenant.name.lowercase().contains(q) ||
                        (tenant.email?.lowercase()?.contains(q) == true) ||
                        (tenant.phone?.lowercase()?.contains(q) == true) ||
                        (tenant.propertyName?.lowercase()?.contains(q) == true) ||
                        (tenant.roomNumber?.lowercase()?.contains(q) == true)
                }

                // 2. Status dropdown filter
                val matchesStatus = when (selectedStatus) {
                    "All statuses" -> true
                    else -> tenant.status.equals(selectedStatus, ignoreCase = true)
                }

                // 3. Property dropdown filter
                val matchesProperty = when (selectedProperty) {
                    "All properties" -> true
                    else -> tenant.propertyName?.equals(selectedProperty, ignoreCase = true) == true
                }

                matchesSearch && matchesStatus && matchesProperty
            }
        }

    val totalCount: Int
        get() = allTenants.size

    val activeCount: Int
        get() = allTenants.count { it.status.equals("Active", ignoreCase = true) }
}

sealed interface TenantsListSideEffect {
    data class NavigateToTenantDetails(val tenantId: String) : TenantsListSideEffect
    data class ShowError(val message: String) : TenantsListSideEffect
    data class ShowMessage(val message: String) : TenantsListSideEffect
}

sealed interface TenantsListAction {
    data class OnSearchQueryChanged(val query: String) : TenantsListAction
    data class OnStatusFilterChanged(val status: String) : TenantsListAction
    data class OnPropertyFilterChanged(val propertyName: String) : TenantsListAction
    data class OnTenantClicked(val tenantId: String) : TenantsListAction
    data object OnRetry : TenantsListAction
}
