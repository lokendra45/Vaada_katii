package com.gaatho.rent.features.payment.domain.repository

import com.gaatho.rent.features.payment.domain.model.Payment
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

/**
 * Repository interface for managing Payments across local and cloud data sources.
 */
interface PaymentRepository {
    fun getPaymentsByOwner(ownerId: String): Flow<List<Payment>>
    fun getPagedPayments(
        ownerId: String,
        searchQuery: String = "",
        statusFilter: String = ""
    ): Flow<PagingData<Payment>>
    fun getPaymentsByTenant(tenantId: String): Flow<List<Payment>>
    fun getPaymentById(paymentId: String): Flow<Payment?>
    
    /** Returns true if a payment already exists for this tenant in the specified year and month. */
    suspend fun checkDuplicatePaymentForMonth(tenantId: String, year: Int, month: Int, excludePaymentId: String? = null): Boolean
    
    suspend fun createPayment(payment: Payment): ApiResponse<Unit>
    suspend fun updatePayment(payment: Payment): ApiResponse<Unit>
    suspend fun deletePayment(paymentId: String): ApiResponse<Unit>
}
