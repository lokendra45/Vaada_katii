package com.gaatho.rent.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.gaatho.rent.database.entity.PropertyEntity
import com.gaatho.rent.database.entity.TenantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: TenantEntity)

    @Query("""
        SELECT * FROM tenant 
        LEFT JOIN property ON tenant.property_id = property.id 
        WHERE tenant.owner_id = :ownerId
    """)
    fun selectTenantsWithProperties(ownerId: String): Flow<Map<PropertyEntity?, List<TenantEntity>>>

    @Query("""
        SELECT * FROM tenant 
        LEFT JOIN property ON tenant.property_id = property.id 
        WHERE tenant.id = :id
    """)
    fun selectTenantWithPropertyById(id: String): Flow<Map<PropertyEntity?, List<TenantEntity>>>

    @Query("DELETE FROM tenant WHERE id = :id")
    suspend fun deleteTenant(id: String)
}

