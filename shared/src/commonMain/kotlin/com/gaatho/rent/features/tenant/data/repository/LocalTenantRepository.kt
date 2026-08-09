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
import com.gaatho.rent.database.entity.TenantWithPropertyName
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.database.security.SecretString
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.PagingSource
import androidx.paging.PagingState

class LocalTenantRepository(
    private val tenantDao: TenantDao
) : TenantRepository {

    override fun getTenants(ownerId: String): Flow<List<Tenant>> {
        return tenantDao.selectTenantsWithProperties(ownerId)
            .map { list ->
                list.map { it.tenant.toDomain(it.propertyName) }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getPagedTenants(
        ownerId: String,
        searchQuery: String,
        statusFilter: String,
        propertyId: String
    ): Flow<PagingData<Tenant>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                tenantDao.selectPagedTenantsWithProperties(
                    ownerId = ownerId,
                    searchQuery = searchQuery,
                    statusFilter = statusFilter,
                    propertyId = propertyId
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { it.tenant.toDomain(it.propertyName) }
        }
    }

    override fun getTenantById(tenantId: String): Flow<Tenant?> {
        return tenantDao.selectTenantWithPropertyById(tenantId)
            .map { pojo ->
                pojo?.tenant?.toDomain(pojo.propertyName)
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
