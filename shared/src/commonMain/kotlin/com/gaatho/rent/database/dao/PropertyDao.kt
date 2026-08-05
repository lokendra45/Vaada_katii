package com.gaatho.rent.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.gaatho.rent.database.entity.PropertyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity)

    @Query("SELECT * FROM property")
    fun selectAllProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM property WHERE owner_id = :ownerId")
    fun selectPropertiesByOwner(ownerId: String): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM property WHERE id = :id")
    fun selectPropertyById(id: String): Flow<PropertyEntity?>

    @Query("DELETE FROM property WHERE id = :id")
    suspend fun deleteProperty(id: String)
}

