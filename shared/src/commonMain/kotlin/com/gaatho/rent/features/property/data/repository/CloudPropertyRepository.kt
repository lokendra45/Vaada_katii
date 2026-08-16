package com.gaatho.rent.features.property.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.gaatho.rent.core.network.runSupabaseWriteUnit
import com.gaatho.rent.core.network.safeSupabaseRead
import com.gaatho.rent.features.property.data.dto.PropertyDto
import com.gaatho.rent.features.property.data.dto.toDomain
import com.gaatho.rent.features.property.data.dto.toDto
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.tenant.data.repository.SupabasePagingSource
import com.skydoves.sandwich.ApiResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class CloudPropertyRepository(
    private val supabase: SupabaseClient,
    private val json: Json
) : PropertyRepository {

    override fun getProperties(ownerId: String): Flow<ImmutableList<Property>> =
        safeSupabaseRead(emptyList<Property>().toPersistentList(), "CloudPropertyRepository.getProperties") {
            val dtos = supabase.postgrest["property"]
                .select {
                    filter {
                        eq("owner_id", ownerId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<PropertyDto>()
            dtos.map { it.toDomain() }.toPersistentList()
        }

    override fun getPagedProperties(
        ownerId: String,
        searchQuery: String,
        locationFilter: String
    ): Flow<PagingData<Property>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = {
            SupabasePagingSource(
                client = supabase,
                table = TABLE,
                json = json,
                serializer = PropertyDto.serializer(),
                orderColumn = "created_at",
                orderDirection = Order.DESCENDING,
                cursorOf = { it.createdAt },
                idOf = { it.id.orEmpty() }
            ) {
                select {
                    filter {
                        eq("owner_id", ownerId)
                        if (searchQuery.isNotBlank()) {
                            ilike("name", "%$searchQuery%")
                        }
                        if (locationFilter.isNotBlank()) {
                            ilike("address", "%$locationFilter%")
                        }
                    }
                }
            }
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toDomain() }
    }

    override fun getPropertyById(propertyId: String): Flow<Property?> =
        safeSupabaseRead(null, "CloudPropertyRepository.getPropertyById") {
            val dto = supabase.postgrest["property"]
                .select {
                    filter {
                        eq("id", propertyId)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<PropertyDto>()
            dto?.toDomain()
        }

    override suspend fun createProperty(property: Property): ApiResponse<Unit> =
        runSupabaseWriteUnit("CloudPropertyRepository.createProperty") {
            supabase.postgrest["property"].insert(property.toDto())
        }

    override suspend fun updateProperty(property: Property): ApiResponse<Unit> =
        runSupabaseWriteUnit("CloudPropertyRepository.updateProperty") {
            supabase.postgrest["property"].update(property.toDto()) {
                filter {
                    eq("id", property.id)
                }
            }
        }

    override suspend fun deleteProperty(propertyId: String): ApiResponse<Unit> =
        runSupabaseWriteUnit("CloudPropertyRepository.deleteProperty") {
            supabase.postgrest["property"].delete {
                filter {
                    eq("id", propertyId)
                }
            }
        }

    private companion object {
        const val TABLE = "property"
        const val PAGE_SIZE = 20
    }
}