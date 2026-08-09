package com.gaatho.rent.features.property.data.repository

import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

/**
 * Repository interface for managing Properties.
 */
interface PropertyRepository {
    fun getProperties(ownerId: String): Flow<List<Property>>
    fun getPagedProperties(
        ownerId: String,
        searchQuery: String = "",
        locationFilter: String = ""
    ): Flow<PagingData<Property>>
    fun getPropertyById(propertyId: String): Flow<Property?>
    suspend fun createProperty(property: Property): ApiResponse<Unit>
    suspend fun updateProperty(property: Property): ApiResponse<Unit>
    suspend fun deleteProperty(propertyId: String): ApiResponse<Unit>
}
