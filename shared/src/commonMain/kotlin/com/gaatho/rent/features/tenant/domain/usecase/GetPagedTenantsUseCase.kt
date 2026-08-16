package com.gaatho.rent.features.tenant.domain.usecase

import androidx.paging.PagingData
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.coroutines.flow.Flow

/**
 * Resolves the UI-level filter selections into repository parameters and returns a
 * paged stream of tenants.
 *
 * Business rules that previously lived inside the list ViewModel:
 * - "All statuses" is the repository's empty-string sentinel for "no status filter".
 * - "All properties" resolves to an empty property id; any other selection is
 *   matched against the loaded [properties] by name to obtain the property id.
 */
class GetPagedTenantsUseCase(
    private val repository: TenantRepository
) {
    operator fun invoke(
        ownerId: String,
        searchQuery: String,
        statusFilter: String,
        propertyFilter: String,
        properties: List<Property>?
    ): Flow<PagingData<Tenant>> {
        val resolvedStatus = if (statusFilter == ALL_STATUSES) "" else statusFilter
        val resolvedPropertyId = if (propertyFilter == ALL_PROPERTIES) {
            ""
        } else {
            properties?.firstOrNull { it.name == propertyFilter }?.id ?: ""
        }

        return repository.getPagedTenants(
            ownerId = ownerId,
            searchQuery = searchQuery,
            statusFilter = resolvedStatus,
            propertyId = resolvedPropertyId
        )
    }

    private companion object {
        const val ALL_STATUSES = "All statuses"
        const val ALL_PROPERTIES = "All properties"
    }
}
