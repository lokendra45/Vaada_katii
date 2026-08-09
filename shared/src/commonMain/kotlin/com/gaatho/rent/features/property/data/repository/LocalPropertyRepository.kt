package com.gaatho.rent.features.property.data.repository

import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.database.dao.PropertyDao
import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.gaatho.rent.database.entity.PropertyEntity
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.database.security.SecretString

class LocalPropertyRepository(
    private val propertyDao: PropertyDao
) : PropertyRepository {

    override fun getProperties(ownerId: String): Flow<List<Property>> {
        return propertyDao.selectPropertiesByOwner(ownerId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getPagedProperties(
        ownerId: String,
        searchQuery: String,
        locationFilter: String
    ): Flow<PagingData<Property>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                propertyDao.selectPagedProperties(
                    ownerId = ownerId,
                    searchQuery = searchQuery,
                    locationFilter = locationFilter
                )
            }
        ).flow
            .map { pagingData ->
                pagingData.map { it.toDomain() }
            }
    }

    override fun getPropertyById(propertyId: String): Flow<Property?> {
        return propertyDao.selectPropertyById(propertyId)
            .map { it?.toDomain() }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun createProperty(property: Property): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                propertyDao.insertProperty(property.toEntity())
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e(e) { "Failed to insert property" }
                ApiResponse.Failure.Exception(e)
            }
        }

    override suspend fun deleteProperty(propertyId: String): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                propertyDao.deleteProperty(propertyId)
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e(e) { "Failed to delete property" }
                ApiResponse.Failure.Exception(e)
            }
        }

    override suspend fun updateProperty(property: Property): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                propertyDao.insertProperty(property.toEntity()) // REPLACE strategy handles updates
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e(e) { "Failed to update property" }
                ApiResponse.Failure.Exception(e)
            }
        }

    private fun PropertyEntity.toDomain() = Property(
        id = id,
        ownerId = ownerId,
        name = name,
        address = address.value,
        imageUrl = imageUrl,
        propertyType = propertyType,
        totalUnits = totalUnits,
        billingCycle = billingCycle,
        amenities = amenities,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Property.toEntity() = PropertyEntity(
        id = id,
        ownerId = ownerId,
        name = name,
        address = SecretString(address),
        imageUrl = imageUrl,
        propertyType = propertyType,
        totalUnits = totalUnits,
        billingCycle = billingCycle,
        amenities = amenities,
        createdAt = createdAt ?: DateTimeUtil.nowIsoString(),
        updatedAt = updatedAt ?: DateTimeUtil.nowIsoString()
    )
}
