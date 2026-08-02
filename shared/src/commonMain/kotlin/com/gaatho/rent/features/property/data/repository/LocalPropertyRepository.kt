package com.gaatho.rent.features.property.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.database.RentManagerDatabase
import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.gaatho.rent.database.Property_ as PropertyEntity
import com.gaatho.rent.core.utils.DateTimeUtil

/**
 * SQLDelight-backed local implementation of [PropertyRepository].
 *
 * ## Threading contract
 * - **Reads**: `.flowOn(Dispatchers.IO)` — callers collect on any dispatcher they choose.
 * - **Writes**: `withContext(Dispatchers.IO)` — callers don't need to specify a dispatcher.
 *
 * ## Error handling
 * Write operations catch all exceptions and return [ApiResponse.Failure.Exception]
 * instead of crashing. The ViewModel receives a typed failure it can surface to the UI.
 */
class LocalPropertyRepository(
    private val database: RentManagerDatabase
) : PropertyRepository {
    private val queries = database.rentManagerQueries

    override fun getProperties(ownerId: String): Flow<List<Property>> {
        return queries.selectPropertiesByOwner(ownerId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun createProperty(property: Property): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                database.transaction {
                    queries.insertProperty(
                        id = property.id,
                        owner_id = property.ownerId,
                        name = property.name,
                        address = property.address,
                        image_url = property.imageUrl,
                        property_type = property.propertyType,
                        created_at = property.createdAt ?: DateTimeUtil.nowIsoString(),
                        updated_at = property.updatedAt ?: DateTimeUtil.nowIsoString()
                    )
                }
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e { "createProperty failed: ${e.message}" }
                ApiResponse.Failure.Exception(e)
            }
        }

    override suspend fun updateProperty(property: Property): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                database.transaction {
                    queries.insertProperty(
                        id = property.id,
                        owner_id = property.ownerId,
                        name = property.name,
                        address = property.address,
                        image_url = property.imageUrl,
                        property_type = property.propertyType,
                        created_at = property.createdAt ?: DateTimeUtil.nowIsoString(),
                        updated_at = property.updatedAt ?: DateTimeUtil.nowIsoString()
                    )
                }
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e { "updateProperty failed: ${e.message}" }
                ApiResponse.Failure.Exception(e)
            }
        }

    override suspend fun deleteProperty(propertyId: String): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                database.transaction { queries.deleteProperty(propertyId) }
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e { "deleteProperty failed: ${e.message}" }
                ApiResponse.Failure.Exception(e)
            }
        }
}

private fun PropertyEntity.toDomain() = Property(
    id = id,
    ownerId = owner_id,
    name = name,
    address = address,
    imageUrl = image_url,
    propertyType = property_type,
    createdAt = created_at,
    updatedAt = updated_at
)
