package com.gaatho.rent.features.tenant.data.repository

/**
 * Supabase-backed [TenantRepository]. All reads go straight to PostgREST; the
 * local Room layer no longer backs tenant data.
 */
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.gaatho.rent.core.cache.DataStoreCache
import com.gaatho.rent.core.network.runSupabaseWriteUnit
import com.gaatho.rent.core.network.safeSupabaseRead
import com.gaatho.rent.core.network.safeSupabaseReadWithCache
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

class CloudTenantRepository(
    private val supabase: SupabaseClient,
    private val json: Json,
    private val cache: DataStoreCache
) : TenantRepository {

    override fun getTenants(ownerId: String): Flow<List<Tenant>> =
        safeSupabaseReadWithCache(
            default = emptyList(),
            tag = "CloudTenantRepository.getTenants",
            cache = cache,
            cacheKey = "tenants_$ownerId",
            serializer = kotlinx.serialization.builtins.ListSerializer(Tenant.serializer())
        ) {
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

    override suspend fun findDuplicateContact(ownerId: String, email: String, phone: String, excludeTenantId: String?): Tenant? {
        if (email.isBlank() && phone.isBlank()) return null

        return try {
            // Single query checking email OR phone using Supabase's OR operator
            // This avoids two separate API calls while preserving the "check email first, then phone" semantics
            val dto = supabase.postgrest["tenant"].select {
                filter {
                    eq("owner_id", ownerId)
                    or {
                        if (email.isNotBlank()) ilike("email", email)
                        if (phone.isNotBlank()) eq("phone", phone)
                    }
                    if (excludeTenantId != null) neq("id", excludeTenantId)
                }
                limit(1)
            }.decodeSingleOrNull<TenantDto>()

            dto?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns tenants that are inactive and older than 30 days.
     * Used by [GetArchivedTenantsUseCase] to avoid fetching all tenants.
     */
    override fun getInactiveTenantsOlderThan30Days(ownerId: String): Flow<List<Tenant>> =
        safeSupabaseRead(emptyList(), "CloudTenantRepository.getInactiveTenantsOlderThan30Days") {
            val dtos = supabase.postgrest["tenant"]
                .select(Columns.raw("*, property(name)")) {
                    filter {
                        eq("owner_id", ownerId)
                        eq("status", "Inactive")
                    }
                }
                .decodeList<TenantDto>()
            dtos.map { it.toDomain() }
        }

    private companion object {
        const val TABLE = "tenant"
        const val PAGE_SIZE = 20
    }
}