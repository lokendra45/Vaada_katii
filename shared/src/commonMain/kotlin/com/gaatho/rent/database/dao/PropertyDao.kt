package com.gaatho.rent.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.gaatho.rent.database.entity.PropertyEntity
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface PropertyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity)

    @Query("SELECT * FROM property")
    fun selectAllProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM property WHERE owner_id = :ownerId")
    fun selectPropertiesByOwner(ownerId: String): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM property WHERE id = :id")
    fun selectPropertyById(id: String): Flow<PropertyEntity?>

    @Query("""
        SELECT * FROM property 
        WHERE owner_id = :ownerId 
        AND (:searchQuery = '' OR name LIKE '%' || :searchQuery || '%' OR address LIKE '%' || :searchQuery || '%')
        AND (:locationFilter = '' OR name LIKE '%' || :locationFilter || '%' OR address LIKE '%' || :locationFilter || '%')
        ORDER BY name ASC
    """)
    fun selectPagedProperties(
        ownerId: String,
        searchQuery: String = "",
        locationFilter: String = ""
    ): PagingSource<Int, PropertyEntity>



    @Query("DELETE FROM property WHERE id = :id")
    suspend fun deleteProperty(id: String)

    @Query("SELECT * FROM property WHERE sync_status = 'PENDING'")
    suspend fun getPendingProperties(): List<PropertyEntity>

    @Query("UPDATE property SET sync_status = 'SYNCED', last_sync_error = NULL WHERE id IN (:ids)")
    suspend fun markPropertiesAsSynced(ids: List<String>)

    @Query("UPDATE property SET last_sync_error = :error WHERE id IN (:ids)")
    suspend fun markPropertiesSyncFailed(ids: List<String>, error: String)
}

