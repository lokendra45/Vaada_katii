package com.gaatho.rent.features.tenant.data.repository

import com.gaatho.rent.features.paywall.data.repository.PaywallRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

/**
 * Routes tenant data operations to either local database (Free) or Supabase cloud (Premium)
 * based on user's RevenueCat subscription status.
 */
class ProxyTenantRepository(
    private val localRepository: LocalTenantRepository,
    private val cloudRepository: CloudTenantRepository,
    private val paywallRepository: PaywallRepository
) : TenantRepository {

    private val activeRepository: TenantRepository
        get() = if (paywallRepository.hasPremiumAccess()) cloudRepository else localRepository

    override fun getTenants(ownerId: String): Flow<List<Tenant>> =
        activeRepository.getTenants(ownerId)

    override fun getPagedTenants(
        ownerId: String,
        searchQuery: String,
        statusFilter: String,
        propertyId: String
    ): Flow<PagingData<Tenant>> = activeRepository.getPagedTenants(ownerId, searchQuery, statusFilter, propertyId)

    override fun getTenantById(tenantId: String): Flow<Tenant?> =
        activeRepository.getTenantById(tenantId)

    override suspend fun createTenant(tenant: Tenant): ApiResponse<Unit> =
        activeRepository.createTenant(tenant)

    override suspend fun updateTenant(tenant: Tenant): ApiResponse<Unit> =
        activeRepository.updateTenant(tenant)

    override suspend fun deleteTenant(tenantId: String): ApiResponse<Unit> =
        activeRepository.deleteTenant(tenantId)
}
