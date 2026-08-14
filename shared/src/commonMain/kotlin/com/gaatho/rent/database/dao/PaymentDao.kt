package com.gaatho.rent.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.gaatho.rent.database.entity.PaymentEntity
import com.gaatho.rent.database.entity.PropertyEntity
import com.gaatho.rent.database.entity.TenantEntity
import com.gaatho.rent.database.projection.PaymentListRow
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<PaymentEntity>)

    // ── Detail/edit screens — still use full entity ──────────────────────────

    @Query("""
        SELECT * FROM payment 
        JOIN tenant ON payment.tenant_id = tenant.id AND tenant.owner_id = payment.owner_id
        LEFT JOIN property ON payment.property_id = property.id AND property.owner_id = payment.owner_id
        WHERE payment.owner_id = :ownerId
        ORDER BY payment.date DESC, payment.id DESC
    """)
    fun selectPaymentsWithDetailsByOwner(ownerId: String): Flow<Map<PaymentEntity, Map<TenantEntity, PropertyEntity?>>>

    @Query("""
        SELECT * FROM payment 
        JOIN tenant ON payment.tenant_id = tenant.id AND tenant.owner_id = payment.owner_id
        LEFT JOIN property ON payment.property_id = property.id AND property.owner_id = payment.owner_id
        WHERE payment.tenant_id = :tenantId
        ORDER BY payment.date DESC, payment.id DESC
    """)
    fun selectPaymentsWithDetailsByTenant(tenantId: String): Flow<Map<PaymentEntity, Map<TenantEntity, PropertyEntity?>>>

    @Query("""
        SELECT * FROM payment 
        JOIN tenant ON payment.tenant_id = tenant.id AND tenant.owner_id = payment.owner_id
        LEFT JOIN property ON payment.property_id = property.id AND property.owner_id = payment.owner_id
        WHERE payment.id = :id
    """)
    fun selectPaymentWithDetailsById(id: String): Flow<Map<PaymentEntity, Map<TenantEntity, PropertyEntity?>>>

    // ── Paginated list — narrow projection + owner-isolated JOIN ─────────────

    @Query("""
        SELECT
            p.id,
            p.owner_id,
            p.tenant_id,
            p.property_id,
            p.amount,
            p.date,
            p.status,
            p.payment_method,
            t.name          AS tenant_name,
            t.room_number   AS tenant_room_number,
            pr.name         AS property_name
        FROM payment AS p
        LEFT JOIN tenant AS t
            ON t.id = p.tenant_id
            AND t.owner_id = p.owner_id
        LEFT JOIN property AS pr
            ON pr.id = p.property_id
            AND pr.owner_id = p.owner_id
        WHERE p.owner_id = :ownerId
          AND p.deleted_at IS NULL
          AND (
              :searchQuery = ''
              OR LOWER(t.name) LIKE '%' || LOWER(:searchQuery) || '%' ESCAPE '\'
              OR LOWER(pr.name) LIKE '%' || LOWER(:searchQuery) || '%' ESCAPE '\'
          )
          AND (
              :statusFilter = ''
              OR p.status = :statusFilter
          )
        ORDER BY
            p.date DESC,
            p.id DESC
    """)
    fun selectPagedPaymentListRows(
        ownerId: String,
        searchQuery: String = "",
        statusFilter: String = ""
    ): PagingSource<Int, PaymentListRow>

    // ── Mutations & sync ─────────────────────────────────────────────────────

    @Query("UPDATE payment SET deleted_at = strftime('%Y-%m-%dT%H:%M:%SZ', 'now') WHERE id = :id")
    suspend fun deletePayment(id: String)

    @Query("SELECT * FROM payment WHERE sync_status = 'PENDING'")
    suspend fun getPendingPayments(): List<PaymentEntity>

    @Query("UPDATE payment SET sync_status = 'SYNCED', last_sync_error = NULL WHERE id IN (:ids)")
    suspend fun markPaymentsAsSynced(ids: List<String>)

    @Query("UPDATE payment SET last_sync_error = :error WHERE id IN (:ids)")
    suspend fun markPaymentsSyncFailed(ids: List<String>, error: String)
}
