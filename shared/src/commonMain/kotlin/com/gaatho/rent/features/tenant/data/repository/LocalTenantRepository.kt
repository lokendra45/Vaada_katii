package com.gaatho.rent.features.tenant.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.database.RentManagerDatabase
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.gaatho.rent.database.Tenant as TenantEntity

class LocalTenantRepository(
    private val database: RentManagerDatabase
) : TenantRepository {
    private val queries = database.rentManagerQueries

    override fun getTenants(ownerId: String): Flow<List<Tenant>> {
        return queries.selectTenantsByOwner(ownerId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun createTenant(tenant: Tenant): ApiResponse<Unit> =
        ApiResponse.suspendOf {
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
                    status = tenant.status,
                    created_at = tenant.createdAt ?: DateTimeUtil.nowIsoString(),
                    updated_at = tenant.updatedAt ?: DateTimeUtil.nowIsoString()
                )
            }
        }.let { ApiResponse.Success(Unit) }

    override suspend fun updateTenant(tenant: Tenant): ApiResponse<Unit> =
        ApiResponse.suspendOf {
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
                    status = tenant.status,
                    created_at = tenant.createdAt ?: DateTimeUtil.nowIsoString(),
                    updated_at = tenant.updatedAt ?: DateTimeUtil.nowIsoString()
                )
            }
        }.let { ApiResponse.Success(Unit) }

    override suspend fun deleteTenant(tenantId: String): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            queries.deleteTenant(tenantId)
        }.let { ApiResponse.Success(Unit) }
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
    status = status,
    createdAt = created_at,
    updatedAt = updated_at
)
