package com.gaatho.rent.features.property.data.repository

import com.gaatho.rent.features.property.data.dto.PropertyDto
import com.gaatho.rent.features.property.data.dto.toDomain
import com.gaatho.rent.features.property.data.dto.toDto
import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.ApiResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import androidx.paging.PagingData
import kotlinx.coroutines.flow.emptyFlow

class CloudPropertyRepository(
    private val supabase: SupabaseClient
) : PropertyRepository {

    override fun getProperties(ownerId: String): Flow<ImmutableList<Property>> = flow {
        val dtos = supabase.postgrest["property"]
            .select {
                filter {
                    eq("owner_id", ownerId)
                }
            }
            .decodeList<PropertyDto>()
        emit(dtos.map { it.toDomain() }.toPersistentList())
    }

    override fun getPagedProperties(
        ownerId: String,
        searchQuery: String,
        locationFilter: String
    ): Flow<PagingData<Property>> {
        // TODO: Implement Supabase cursor-based pagination
        return emptyFlow()
    }

    override fun getPropertyById(propertyId: String): Flow<Property?> {
        return kotlinx.coroutines.flow.flowOf(null)
    }

    override suspend fun createProperty(property: Property): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            supabase.postgrest["property"].insert(property.toDto())
        }.let { ApiResponse.Success(Unit) }

    override suspend fun updateProperty(property: Property): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            supabase.postgrest["property"].update(property.toDto()) {
                filter {
                    eq("id", property.id)
                }
            }
        }.let { ApiResponse.Success(Unit) }

    override suspend fun deleteProperty(propertyId: String): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            supabase.postgrest["property"].delete {
                filter {
                    eq("id", propertyId)
                }
            }
        }.let { ApiResponse.Success(Unit) }
}
