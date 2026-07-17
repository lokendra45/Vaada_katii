package com.gaatho.rent.features.property.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gaatho.rent.database.RentManagerDatabase
import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.gaatho.rent.database.Property_ as PropertyEntity

class LocalPropertyRepository(
    private val database: RentManagerDatabase
) : PropertyRepository {
    private val queries = database.rentManagerQueries

    override fun getProperties(ownerId: String): Flow<List<Property>> {
        return queries.selectPropertiesByOwner(ownerId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun createProperty(property: Property): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            database.transaction {
                queries.insertProperty(
                    id = property.id,
                    owner_id = property.ownerId,
                    name = property.name,
                    address = property.address,
                    image_url = property.imageUrl,
                    property_type = property.propertyType,
                    created_at = property.createdAt,
                    updated_at = property.updatedAt
                )
            }
        }.let { ApiResponse.Success(Unit) }

    override suspend fun updateProperty(property: Property): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            database.transaction {
                queries.insertProperty(
                    id = property.id,
                    owner_id = property.ownerId,
                    name = property.name,
                    address = property.address,
                    image_url = property.imageUrl,
                    property_type = property.propertyType,
                    created_at = property.createdAt,
                    updated_at = property.updatedAt
                )
            }
        }.let { ApiResponse.Success(Unit) }

    override suspend fun deleteProperty(propertyId: String): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            database.transaction {
                queries.deleteProperty(propertyId)
            }
        }.let { ApiResponse.Success(Unit) }
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
