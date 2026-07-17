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
