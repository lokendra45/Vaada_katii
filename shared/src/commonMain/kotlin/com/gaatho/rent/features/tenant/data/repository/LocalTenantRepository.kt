package com.gaatho.rent.features.tenant.data.repository

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
import com.gaatho.rent.database.dao.TenantDao
import com.gaatho.rent.database.entity.TenantEntity
import com.gaatho.rent.database.projection.TenantListRow
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalTenantRepository(
    private val database: AppDatabase,
    private val tenantDao: TenantDao
) : TenantRepository {

    override fun getTenants(ownerId: String): Flow<List<Tenant>> {
        return tenantDao.selectTenantsWithProperties(ownerId)
            .map { list ->
                list.filter { it.tenant.deletedAt == null }.map { it.tenant.toDomain(it.propertyName) }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getPagedTenants(
        ownerId: String,
        searchQuery: String,
        statusFilter: String,
        propertyId: String
    ): Flow<PagingData<Tenant>> {
        val normalizedSearch = StringUtil.escapeLike(StringUtil.normalizeSearch(searchQuery))

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = true,
                maxSize = 100
            ),
            pagingSourceFactory = {
                tenantDao.selectPagedTenantListRows(
                    ownerId = ownerId,
                    searchQuery = normalizedSearch,
                    statusFilter = statusFilter,
                    propertyId = propertyId
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { row -> row.toDomain() }
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

    /** Maps narrow [TenantListRow] projection (for list screens). */
    private fun TenantListRow.toDomain() = Tenant(
        id = id,
        ownerId = ownerId,
        propertyId = propertyId,
        propertyName = propertyName,
        name = name,
        status = status,
        roomNumber = roomNumber,
        rentAmount = rentAmount
    )

    /** Maps full [TenantEntity] (for detail/edit screens where email/phone are shown). */
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
        createdAt = IsoDateUtil.normalize(createdAt ?: DateTimeUtil.nowIsoString()) ?: DateTimeUtil.nowIsoString(),
        updatedAt = IsoDateUtil.normalize(updatedAt ?: DateTimeUtil.nowIsoString()) ?: DateTimeUtil.nowIsoString(),
        syncStatus = "PENDING"
    )
}

