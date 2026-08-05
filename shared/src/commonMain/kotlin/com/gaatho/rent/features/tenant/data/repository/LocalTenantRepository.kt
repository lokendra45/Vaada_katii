package com.gaatho.rent.features.tenant.data.repository

import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.database.dao.TenantDao
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.gaatho.rent.database.entity.TenantEntity
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.database.security.SecretString

class LocalTenantRepository(
    private val tenantDao: TenantDao
) : TenantRepository {

    override fun getTenants(ownerId: String): Flow<List<Tenant>> {
        return tenantDao.selectTenantsWithProperties(ownerId)
            .map { map ->
                map.flatMap { (property, tenants) ->
                    tenants.map { it.toDomain(property?.name) }
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getTenantById(tenantId: String): Flow<Tenant?> {
        return tenantDao.selectTenantWithPropertyById(tenantId)
            .map { map ->
                map.flatMap { (property, tenants) ->
                    tenants.map { it.toDomain(property?.name) }
                }.firstOrNull()
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun createTenant(tenant: Tenant): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                tenantDao.insertTenant(tenant.toEntity())
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e(e) { "Failed to insert tenant" }
                ApiResponse.Failure.Exception(e)
            }
        }

    override suspend fun deleteTenant(tenantId: String): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                tenantDao.deleteTenant(tenantId)
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e(e) { "Failed to delete tenant" }
                ApiResponse.Failure.Exception(e)
            }
        }

    override suspend fun updateTenant(tenant: Tenant): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                tenantDao.insertTenant(tenant.toEntity())
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e(e) { "Failed to update tenant" }
                ApiResponse.Failure.Exception(e)
            }
        }

    private fun TenantEntity.toDomain(resolvedPropertyName: String?) = Tenant(
        id = id,
        ownerId = ownerId,
        name = name,
        email = email?.value,
        phone = phone?.value,
        propertyId = propertyId,
        propertyName = resolvedPropertyName,
        roomNumber = roomNumber,
        rentAmount = rentAmount,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Tenant.toEntity() = TenantEntity(
        id = id,
        ownerId = ownerId,
        name = name,
        email = email?.let { SecretString(it) },
        phone = phone?.let { SecretString(it) },
        propertyId = propertyId,
        roomNumber = roomNumber,
        rentAmount = rentAmount,
        status = status,
        createdAt = createdAt ?: DateTimeUtil.nowIsoString(),
        updatedAt = updatedAt ?: DateTimeUtil.nowIsoString()
    )
}
