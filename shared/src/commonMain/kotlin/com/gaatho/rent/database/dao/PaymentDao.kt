package com.gaatho.rent.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.gaatho.rent.database.entity.PaymentEntity
import com.gaatho.rent.database.entity.PropertyEntity
import com.gaatho.rent.database.entity.TenantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Query("""
        SELECT * FROM payment 
        JOIN tenant ON payment.tenant_id = tenant.id 
        LEFT JOIN property ON payment.property_id = property.id 
        WHERE payment.owner_id = :ownerId
        ORDER BY payment.date DESC
    """)
    fun selectPaymentsWithDetailsByOwner(ownerId: String): Flow<Map<PaymentEntity, Map<TenantEntity, PropertyEntity?>>>

    @Query("""
        SELECT * FROM payment 
        JOIN tenant ON payment.tenant_id = tenant.id 
        LEFT JOIN property ON payment.property_id = property.id 
        WHERE payment.tenant_id = :tenantId
        ORDER BY payment.date DESC
    """)
    fun selectPaymentsWithDetailsByTenant(tenantId: String): Flow<Map<PaymentEntity, Map<TenantEntity, PropertyEntity?>>>

    @Query("""
        SELECT * FROM payment 
        JOIN tenant ON payment.tenant_id = tenant.id 
        LEFT JOIN property ON payment.property_id = property.id 
        WHERE payment.id = :id
    """)
    fun selectPaymentWithDetailsById(id: String): Flow<Map<PaymentEntity, Map<TenantEntity, PropertyEntity?>>>

    @Query("DELETE FROM payment WHERE id = :id")
    suspend fun deletePayment(id: String)
}

