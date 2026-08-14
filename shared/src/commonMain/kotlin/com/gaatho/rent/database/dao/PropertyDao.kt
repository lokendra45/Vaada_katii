package com.gaatho.rent.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.gaatho.rent.database.entity.PropertyEntity
import com.gaatho.rent.database.projection.PropertyListRow
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface PropertyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(properties: List<PropertyEntity>)

    // ── Detail/edit screens — still use full entity ──────────────────────────

    @Query("SELECT * FROM property")
    fun selectAllProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM property WHERE owner_id = :ownerId ORDER BY name ASC, id ASC")
    fun selectPropertiesByOwner(ownerId: String): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM property WHERE id = :id")
    fun selectPropertyById(id: String): Flow<PropertyEntity?>

    // ── Paginated list — narrow projection + SQL aggregation ─────────────────
    // occupiedUnits and pendingAmount come from SQL COUNT/SUM.
    // JOIN is owner-isolated: t.owner_id = p.owner_id.
    // No tenant table loaded into Kotlin memory.

    @Query("""
        SELECT
            p.id,
            p.owner_id,
            p.name,
            p.address,
            p.image_url,
            p.property_type,
            p.total_units,
            p.monthly_rent,
            COUNT(
                CASE
                    WHEN t.status IN ('Active', 'Overdue') AND t.deleted_at IS NULL
                    THEN 1
                END
            )                AS occupied_units,
            COALESCE(
                SUM(
                    CASE
                        WHEN t.status = 'Overdue' AND t.deleted_at IS NULL
                        THEN t.rent_amount
                        ELSE 0
                    END
                ),
                0
            )                AS pending_amount
        FROM property AS p
        LEFT JOIN tenant AS t
            ON t.property_id = p.id
            AND t.owner_id   = p.owner_id
        WHERE p.owner_id = :ownerId
          AND p.deleted_at IS NULL
          AND (
              :searchQuery = ''
              OR LOWER(p.name) LIKE '%' || LOWER(:searchQuery) || '%' ESCAPE '\'
              OR LOWER(p.address) LIKE '%' || LOWER(:searchQuery) || '%' ESCAPE '\'
          )
          AND (
              :locationFilter = ''
              OR LOWER(p.name) LIKE '%' || LOWER(:locationFilter) || '%' ESCAPE '\'
              OR LOWER(p.address) LIKE '%' || LOWER(:locationFilter) || '%' ESCAPE '\'
          )
        GROUP BY p.id
        ORDER BY
            p.name ASC,
            p.id   ASC
    """)
    fun selectPagedPropertyListRows(
        ownerId: String,
        searchQuery: String = "",
        locationFilter: String = ""
    ): PagingSource<Int, PropertyListRow>

    // ── Mutations & sync ─────────────────────────────────────────────────────

    @Query("UPDATE property SET deleted_at = strftime('%Y-%m-%dT%H:%M:%SZ', 'now') WHERE id = :id")
    suspend fun deleteProperty(id: String)

    @Query("SELECT * FROM property WHERE sync_status = 'PENDING'")
    suspend fun getPendingProperties(): List<PropertyEntity>

    @Query("UPDATE property SET sync_status = 'SYNCED', last_sync_error = NULL WHERE id IN (:ids)")
    suspend fun markPropertiesAsSynced(ids: List<String>)

    @Query("UPDATE property SET last_sync_error = :error WHERE id IN (:ids)")
    suspend fun markPropertiesSyncFailed(ids: List<String>, error: String)
}
