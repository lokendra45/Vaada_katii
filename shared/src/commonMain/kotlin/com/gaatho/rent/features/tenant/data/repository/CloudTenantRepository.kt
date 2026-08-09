package com.gaatho.rent.features.tenant.data.repository

import com.gaatho.rent.features.tenant.data.dto.TenantDto
import com.gaatho.rent.features.tenant.data.dto.toDomain
import com.gaatho.rent.features.tenant.data.dto.toDto
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import androidx.paging.PagingData
import kotlinx.coroutines.flow.emptyFlow

class CloudTenantRepository(
    private val supabase: SupabaseClient
) : TenantRepository {

    override fun getTenants(ownerId: String): Flow<List<Tenant>> = flow {
        val dtos = supabase.postgrest["tenant"]
            .select {
                filter {
                    eq("owner_id", ownerId)
                }
            }
            .decodeList<TenantDto>()
        emit(dtos.map { it.toDomain() })
    }

    override fun getPagedTenants(
        ownerId: String,
        searchQuery: String,
        statusFilter: String,
        propertyId: String
    ): Flow<PagingData<Tenant>> {
        // TODO: Implement Supabase cursor-based pagination or offset pagination
        return emptyFlow()
    }

    override fun getTenantById(tenantId: String): Flow<Tenant?> {
        return kotlinx.coroutines.flow.flowOf(null)
    }

    override suspend fun createTenant(tenant: Tenant): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            supabase.postgrest["tenant"].insert(tenant.toDto())
        }.let { ApiResponse.Success(Unit) }

    override suspend fun updateTenant(tenant: Tenant): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            supabase.postgrest["tenant"].update(tenant.toDto()) {
                filter {
                    eq("id", tenant.id)
                }
            }
        }.let { ApiResponse.Success(Unit) }

    override suspend fun deleteTenant(tenantId: String): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            supabase.postgrest["tenant"].delete {
                filter {
                    eq("id", tenantId)
                }
            }
        }.let { ApiResponse.Success(Unit) }
}
