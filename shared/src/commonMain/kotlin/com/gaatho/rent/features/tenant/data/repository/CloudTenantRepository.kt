package com.gaatho.rent.features.tenant.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.gaatho.rent.core.network.runSupabaseWriteUnit
import com.gaatho.rent.core.network.safeSupabaseRead
import com.gaatho.rent.features.tenant.data.dto.TenantDto
import com.gaatho.rent.features.tenant.data.dto.toDomain
import com.gaatho.rent.features.tenant.data.dto.toDto
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Supabase-backed [TenantRepository]. All reads go straight to PostgREST; the
 * local Room layer no longer backs tenant data.
 */
class CloudTenantRepository(
    private val supabase: SupabaseClient,
    private val json: Json
) : TenantRepository {

    override fun getTenants(ownerId: String): Flow<List<Tenant>> =
        safeSupabaseRead(emptyList(), "CloudTenantRepository.getTenants") {
            val dtos = supabase.postgrest["tenant"]
                .select(Columns.raw("*, property(name)")) {
                    filter {
                        eq("owner_id", ownerId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<TenantDto>()
            dtos.map { it.toDomain() }
        }

    override fun getTenantsByProperty(ownerId: String, propertyId: String): Flow<List<Tenant>> =
        safeSupabaseRead(emptyList(), "CloudTenantRepository.getTenantsByProperty") {
            val dtos = supabase.postgrest["tenant"]
                .select(Columns.raw("*, property(name)")) {
                    filter {
                        eq("owner_id", ownerId)
                        eq("property_id", propertyId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<TenantDto>()
            dtos.map { it.toDomain() }
        }

    override fun getPagedTenants(
        ownerId: String,
        searchQuery: String,
        statusFilter: String,
        propertyId: String
    ): Flow<PagingData<Tenant>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = {
            SupabasePagingSource(
                client = supabase,
                table = TABLE,
                json = json,
                serializer = TenantDto.serializer(),
                orderColumn = "created_at",
                orderDirection = Order.DESCENDING,
                cursorOf = { it.createdAt },
                idOf = { it.id.orEmpty() }
            ) {
                select(Columns.raw("*, property(name)")) {
                    filter {
                        eq("owner_id", ownerId)
                        if (searchQuery.isNotBlank()) {
                            ilike("name", "%$searchQuery%")
                        }
                        if (statusFilter.isNotBlank()) {
                            eq("status", statusFilter)
                        }
                        if (propertyId.isNotBlank()) {
                            eq("property_id", propertyId)
                        }
                    }
                }
            }
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toDomain() }
    }

    override fun getTenantById(tenantId: String): Flow<Tenant?> =
        safeSupabaseRead(null, "CloudTenantRepository.getTenantById") {
            val dto = supabase.postgrest["tenant"]
                .select(Columns.raw("*, property(name)")) {
                    filter {
                        eq("id", tenantId)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<TenantDto>()
            dto?.toDomain()
        }

    override suspend fun createTenant(tenant: Tenant): ApiResponse<Unit> =
        runSupabaseWriteUnit("CloudTenantRepository.createTenant") {
            supabase.postgrest["tenant"].insert(tenant.toDto())
        }

    override suspend fun updateTenant(tenant: Tenant): ApiResponse<Unit> =
        runSupabaseWriteUnit("CloudTenantRepository.updateTenant") {
            supabase.postgrest["tenant"].update(tenant.toDto()) {
                filter {
                    eq("id", tenant.id)
                }
            }
        }

    override suspend fun deleteTenant(tenantId: String): ApiResponse<Unit> =
        runSupabaseWriteUnit("CloudTenantRepository.deleteTenant") {
            supabase.postgrest["tenant"].delete {
                filter {
                    eq("id", tenantId)
                }
            }
        }

    private companion object {
        const val TABLE = "tenant"
        const val PAGE_SIZE = 20
    }
}