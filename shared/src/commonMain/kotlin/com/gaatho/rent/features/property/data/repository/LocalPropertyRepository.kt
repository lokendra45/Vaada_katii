package com.gaatho.rent.features.property.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.gaatho.rent.core.database.security.SecretString
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.utils.IsoDateUtil
import com.gaatho.rent.core.utils.StringUtil
import com.gaatho.rent.database.AppDatabase
import com.gaatho.rent.database.dao.PropertyDao
import com.gaatho.rent.database.entity.PropertyEntity
import com.gaatho.rent.database.projection.PropertyListRow
import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalPropertyRepository(
    private val database: AppDatabase,
    private val propertyDao: PropertyDao
) : PropertyRepository {

    override fun getProperties(ownerId: String): Flow<List<Property>> {
        return propertyDao.selectPropertiesByOwner(ownerId)
            .map { entities -> 
                // Repository layer filters out soft-deleted records for domain consumers
                entities.filter { it.deletedAt == null }.map { it.toDomain() } 
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getPagedProperties(
        ownerId: String,
        searchQuery: String,
        locationFilter: String
    ): Flow<PagingData<Property>> {
        // Search normalization and wildcard escaping for Postgres-ready SQL
        val normalizedSearch = StringUtil.escapeLike(StringUtil.normalizeSearch(searchQuery))
        val normalizedLocation = StringUtil.escapeLike(StringUtil.normalizeSearch(locationFilter))

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = true,
                maxSize = 100
            ),
            pagingSourceFactory = {
                propertyDao.selectPagedPropertyListRows(
                    ownerId = ownerId,
                    searchQuery = normalizedSearch,
                    locationFilter = normalizedLocation
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { row -> row.toDomain() }
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

    override suspend fun updateProperty(property: Property): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                propertyDao.insertProperty(property.toEntity())
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e(e) { "Failed to update property" }
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

    // ── Mappers ──────────────────────────────────────────────────────────────

    /**
     * Maps the narrow [PropertyListRow] projection to the [Property] domain model.
     * occupiedUnits and pendingAmount come from SQL aggregation (COUNT/SUM).
     */
    private fun PropertyListRow.toDomain() = Property(
        id = id,
        ownerId = ownerId,
        name = name,
        address = address?.value ?: "",
        imageUrl = imageUrl,
        propertyType = propertyType,
        totalUnits = totalUnits,
        monthlyRent = monthlyRent,
        occupiedUnits = occupiedUnits,
        pendingAmount = pendingAmount
    )

    /** Maps full [PropertyEntity] for detail/edit screens. */
    private fun PropertyEntity.toDomain() = Property(
        id = id,
        ownerId = ownerId,
        name = name,
        address = address.value,
        imageUrl = imageUrl,
        propertyType = propertyType,
        totalUnits = totalUnits,
        monthlyRent = monthlyRent,
        description = description,
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
        monthlyRent = monthlyRent,
        description = description,
        billingCycle = billingCycle,
        amenities = amenities,
        // ISO normalization at the write boundary
        createdAt = IsoDateUtil.normalize(createdAt ?: DateTimeUtil.nowIsoString()) ?: DateTimeUtil.nowIsoString(),
        updatedAt = IsoDateUtil.normalize(updatedAt ?: DateTimeUtil.nowIsoString()) ?: DateTimeUtil.nowIsoString(),
        syncStatus = "PENDING"
    )
}
