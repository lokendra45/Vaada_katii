package com.gaatho.rent.features.tenant.data.repository

import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow

import androidx.paging.PagingData

/**
 * Repository interface for managing Tenants across local and cloud data sources.
 */
interface TenantRepository {
    fun getTenants(ownerId: String): Flow<List<Tenant>>

    /** Returns only the tenants of a specific [propertyId] — filtered server-side. */
    fun getTenantsByProperty(ownerId: String, propertyId: String): Flow<List<Tenant>>

    fun getPagedTenants(
        ownerId: String,
        searchQuery: String = "",
        statusFilter: String = "",
        propertyId: String = ""
    ): Flow<PagingData<Tenant>>
    fun getTenantById(tenantId: String): Flow<Tenant?>
    suspend fun createTenant(tenant: Tenant): ApiResponse<Unit>
    suspend fun updateTenant(tenant: Tenant): ApiResponse<Unit>
    suspend fun deleteTenant(tenantId: String): ApiResponse<Unit>
}
