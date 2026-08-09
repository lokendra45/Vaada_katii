package com.gaatho.rent.features.property.data.repository

import com.gaatho.rent.features.paywall.data.repository.PaywallRepository
import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

/**
 * Routes property data operations to either the local database (Free)
 * or Supabase cloud (Premium) based on the user's RevenueCat entitlement status.
 *
 * Free users:    reads/writes go to SQLDelight only. No network required.
 * Premium users: reads/writes go directly to Supabase. No local DB writes.
 */
class ProxyPropertyRepository(
    private val localRepository: LocalPropertyRepository,
    private val cloudRepository: CloudPropertyRepository,
    private val paywallRepository: PaywallRepository
) : PropertyRepository {

    private val activeRepository: PropertyRepository
        get() = if (paywallRepository.hasPremiumAccess()) cloudRepository else localRepository

    override fun getProperties(ownerId: String): Flow<List<Property>> =
        activeRepository.getProperties(ownerId)

    override fun getPagedProperties(
        ownerId: String,
        searchQuery: String,
        locationFilter: String
    ): Flow<PagingData<Property>> = activeRepository.getPagedProperties(ownerId, searchQuery, locationFilter)

    override fun getPropertyById(propertyId: String): Flow<Property?> =
        activeRepository.getPropertyById(propertyId)

    override suspend fun createProperty(property: Property): ApiResponse<Unit> =
        activeRepository.createProperty(property)

    override suspend fun updateProperty(property: Property): ApiResponse<Unit> =
        activeRepository.updateProperty(property)

    override suspend fun deleteProperty(propertyId: String): ApiResponse<Unit> =
        activeRepository.deleteProperty(propertyId)
}
