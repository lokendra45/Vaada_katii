package com.gaatho.rent.features.property.data.repository

import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow

/**
 * Interface for Property data operations.
 *
 * Implemented by:
 * - [LocalPropertyRepository]: Uses SQLDelight (for Free users)
 * - [CloudPropertyRepository]: Uses Supabase (for Premium users)
 */
interface PropertyRepository {
    fun getProperties(ownerId: String): Flow<List<Property>>
    suspend fun createProperty(property: Property): ApiResponse<Unit>
    suspend fun updateProperty(property: Property): ApiResponse<Unit>
    suspend fun deleteProperty(propertyId: String): ApiResponse<Unit>
}
