package com.gaatho.rent.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.gaatho.rent.database.entity.TenantEntity
import com.gaatho.rent.database.entity.TenantWithPropertyName
import com.gaatho.rent.database.projection.TenantListRow
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface TenantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: TenantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tenants: List<TenantEntity>)

    // ── Detail screens — use full entity with JOIN ───────────────────────────

    @Query("""
        SELECT tenant.*, property.name AS property_name 
        FROM tenant 
        LEFT JOIN property
            ON tenant.property_id = property.id
            AND property.owner_id = tenant.owner_id
        WHERE tenant.owner_id = :ownerId
          AND tenant.deleted_at IS NULL
        ORDER BY tenant.name ASC, tenant.id ASC
    """)
    fun selectTenantsWithProperties(ownerId: String): Flow<List<TenantWithPropertyName>>

    @Query("""
        SELECT tenant.*, property.name AS property_name 
        FROM tenant 
        LEFT JOIN property
            ON tenant.property_id = property.id
            AND property.owner_id = tenant.owner_id
        WHERE tenant.id = :id
          AND tenant.deleted_at IS NULL
    """)
    fun selectTenantWithPropertyById(id: String): Flow<TenantWithPropertyName?>

    // ── Paginated list — narrow projection + owner-isolated JOIN ─────────────
    // Does NOT load encrypted email/phone columns.
    // tenant.email LIKE search is still on the DB side — uses index scan on name,
    // then a separate scan on email which is acceptable for current data size.
    // For 10k+ records, replace email-search with an FTS5 virtual table.

    @Query("""
        SELECT
            t.id,
            t.owner_id,
            t.property_id,
            t.name,
            t.status,
            t.room_number,
            t.rent_amount,
            p.name AS property_name
        FROM tenant AS t
        LEFT JOIN property AS p
            ON p.id       = t.property_id
            AND p.owner_id = t.owner_id
        WHERE t.owner_id = :ownerId
          AND t.deleted_at IS NULL
          AND (
              :searchQuery = ''
              OR LOWER(t.name) LIKE '%' || LOWER(:searchQuery) || '%' ESCAPE '\'
          )
          AND (
              :statusFilter = ''
              OR t.status = :statusFilter
          )
          AND (
              :propertyId = ''
              OR t.property_id = :propertyId
          )
        ORDER BY
            t.name ASC,
            t.id   ASC
    """)
    fun selectPagedTenantListRows(
        ownerId: String,
        searchQuery: String = "",
        statusFilter: String = "",
        propertyId: String = ""
    ): PagingSource<Int, TenantListRow>

    // ── Mutations & sync ─────────────────────────────────────────────────────

    @Query("UPDATE tenant SET deleted_at = strftime('%Y-%m-%dT%H:%M:%SZ', 'now') WHERE id = :id")
    suspend fun deleteTenant(id: String)

    @Query("SELECT * FROM tenant WHERE sync_status = 'PENDING'")
    suspend fun getPendingTenants(): List<TenantEntity>

    @Query("UPDATE tenant SET sync_status = 'SYNCED', last_sync_error = NULL WHERE id IN (:ids)")
    suspend fun markTenantsAsSynced(ids: List<String>)

    @Query("UPDATE tenant SET last_sync_error = :error WHERE id IN (:ids)")
    suspend fun markTenantsSyncFailed(ids: List<String>, error: String)
}
