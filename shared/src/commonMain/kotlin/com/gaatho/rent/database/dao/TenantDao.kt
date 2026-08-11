package com.gaatho.rent.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.gaatho.rent.database.entity.PropertyEntity
import com.gaatho.rent.database.entity.TenantEntity
import androidx.paging.PagingSource
import com.gaatho.rent.database.entity.TenantWithPropertyName
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface TenantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: TenantEntity)

    @Query("""
        SELECT tenant.*, property.name AS property_name 
        FROM tenant 
        LEFT JOIN property ON tenant.property_id = property.id 
        WHERE tenant.owner_id = :ownerId
    """)
    fun selectTenantsWithProperties(ownerId: String): Flow<List<TenantWithPropertyName>>

    @Query("""
        SELECT tenant.*, property.name AS property_name 
        FROM tenant 
        LEFT JOIN property ON tenant.property_id = property.id
        WHERE tenant.id = :id
    """)
    fun selectTenantWithPropertyById(id: String): Flow<TenantWithPropertyName?>

    @Query("""
        SELECT tenant.*, property.name AS property_name 
        FROM tenant 
        LEFT JOIN property ON tenant.property_id = property.id
        WHERE tenant.owner_id = :ownerId 
        AND (:searchQuery = '' OR tenant.name LIKE '%' || :searchQuery || '%' OR tenant.email LIKE '%' || :searchQuery || '%')
        AND (:statusFilter = '' OR tenant.status = :statusFilter)
        AND (:propertyId = '' OR tenant.property_id = :propertyId)
        ORDER BY tenant.name ASC
    """)
    fun selectPagedTenantsWithProperties(
        ownerId: String,
        searchQuery: String = "",
        statusFilter: String = "",
        propertyId: String = ""
    ): PagingSource<Int, TenantWithPropertyName>



    @Query("DELETE FROM tenant WHERE id = :id")
    suspend fun deleteTenant(id: String)

    @Query("SELECT * FROM tenant WHERE sync_status = 'PENDING'")
    suspend fun getPendingTenants(): List<TenantEntity>

    @Query("UPDATE tenant SET sync_status = 'SYNCED', last_sync_error = NULL WHERE id IN (:ids)")
    suspend fun markTenantsAsSynced(ids: List<String>)

    @Query("UPDATE tenant SET last_sync_error = :error WHERE id IN (:ids)")
    suspend fun markTenantsSyncFailed(ids: List<String>, error: String)
}

