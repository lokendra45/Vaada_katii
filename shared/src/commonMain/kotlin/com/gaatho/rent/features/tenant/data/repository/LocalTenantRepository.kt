package com.gaatho.rent.features.tenant.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.database.RentManagerDatabase
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.gaatho.rent.database.Tenant as TenantEntity

/**
 * SQLDelight-backed local implementation of [TenantRepository].
 *
 * ## Threading contract
 * - **Reads**: `.flowOn(Dispatchers.IO)` — callers collect on any dispatcher they choose.
 * - **Writes**: `withContext(Dispatchers.IO)` — callers don't need to specify a dispatcher.
 *
 * ## Error handling
 * Write operations catch all exceptions and return [ApiResponse.Failure.Exception]
 * instead of crashing. The ViewModel receives a typed failure it can surface to the UI.
 */
class LocalTenantRepository(
    private val database: RentManagerDatabase
) : TenantRepository {
    private val queries = database.rentManagerQueries

    override fun getTenants(ownerId: String): Flow<List<Tenant>> {
        return queries.selectTenantsByOwner(ownerId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun createTenant(tenant: Tenant): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                database.transaction {
                    queries.insertTenant(
                        id = tenant.id,
                        owner_id = tenant.ownerId,
                        name = tenant.name,
                        email = tenant.email,
                        phone = tenant.phone,
                        property_id = tenant.propertyId,
                        property_name = tenant.propertyName,
                        room_number = tenant.roomNumber,
                        rent_amount = tenant.rentAmount,
                        status = tenant.status,
                        created_at = tenant.createdAt ?: DateTimeUtil.nowIsoString(),
                        updated_at = tenant.updatedAt ?: DateTimeUtil.nowIsoString()
                    )
                }
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e { "createTenant failed: ${e.message}" }
                ApiResponse.Failure.Exception(e)
            }
        }

    override suspend fun updateTenant(tenant: Tenant): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                database.transaction {
                    queries.insertTenant(
                        id = tenant.id,
                        owner_id = tenant.ownerId,
                        name = tenant.name,
                        email = tenant.email,
                        phone = tenant.phone,
                        property_id = tenant.propertyId,
                        property_name = tenant.propertyName,
                        room_number = tenant.roomNumber,
                        rent_amount = tenant.rentAmount,
                        status = tenant.status,
                        created_at = tenant.createdAt ?: DateTimeUtil.nowIsoString(),
                        updated_at = tenant.updatedAt ?: DateTimeUtil.nowIsoString()
                    )
                }
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e { "updateTenant failed: ${e.message}" }
                ApiResponse.Failure.Exception(e)
            }
        }

    override suspend fun deleteTenant(tenantId: String): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                database.transaction { queries.deleteTenant(tenantId) }
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e { "deleteTenant failed: ${e.message}" }
                ApiResponse.Failure.Exception(e)
            }
        }
}

private fun TenantEntity.toDomain() = Tenant(
    id = id,
    ownerId = owner_id,
    name = name,
    email = email,
    phone = phone,
    propertyId = property_id,
    propertyName = property_name,
    roomNumber = room_number,
    rentAmount = rent_amount,
    status = status,
    createdAt = created_at,
    updatedAt = updated_at
)
