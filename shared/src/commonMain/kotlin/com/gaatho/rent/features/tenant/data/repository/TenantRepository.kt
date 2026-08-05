package com.gaatho.rent.features.tenant.data.repository

import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing Tenants across local and cloud data sources.
 */
interface TenantRepository {
    fun getTenants(ownerId: String): Flow<List<Tenant>>
    fun getTenantById(tenantId: String): Flow<Tenant?>
    suspend fun createTenant(tenant: Tenant): ApiResponse<Unit>
    suspend fun updateTenant(tenant: Tenant): ApiResponse<Unit>
    suspend fun deleteTenant(tenantId: String): ApiResponse<Unit>
}
