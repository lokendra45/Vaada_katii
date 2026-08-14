package com.gaatho.rent.features.payment.data.repository

import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import androidx.paging.PagingData

@OptIn(ExperimentalCoroutinesApi::class)
class ProxyPaymentRepository(
    private val local: LocalPaymentRepository,
    private val cloud: CloudPaymentRepository,
    private val sessionManager: SessionManager
) : PaymentRepository {

    override fun getPaymentsByOwner(ownerId: String): Flow<List<Payment>> {
        return sessionManager.isLoggedIn.flatMapLatest { isLoggedIn ->
            // For now, prioritize local database as source of truth.
            // Cloud sync logic will be added here later (e.g. fetching from cloud and saving to local).
            local.getPaymentsByOwner(ownerId)
        }
    }

    override fun getPagedPayments(
        ownerId: String,
        searchQuery: String,
        statusFilter: String
    ): Flow<PagingData<Payment>> {
        // Paged queries always use local DB (offline-first).
        // Cloud data is synced into local DB separately.
        return local.getPagedPayments(ownerId, searchQuery, statusFilter)
    }

    override fun getPaymentsByTenant(tenantId: String): Flow<List<Payment>> {
        return sessionManager.isLoggedIn.flatMapLatest { isLoggedIn ->
            local.getPaymentsByTenant(tenantId)
        }
    }

    override fun getPaymentById(paymentId: String): Flow<Payment?> {
        return local.getPaymentById(paymentId)
    }

    override suspend fun createPayment(payment: Payment): ApiResponse<Unit> {
        val localResult = local.createPayment(payment)
        if (localResult is ApiResponse.Success && sessionManager.isLoggedIn.value) {
            // Best effort cloud sync
            cloud.createPayment(payment)
        }
        return localResult
    }

    override suspend fun updatePayment(payment: Payment): ApiResponse<Unit> {
        val localResult = local.updatePayment(payment)
        if (localResult is ApiResponse.Success && sessionManager.isLoggedIn.value) {
            cloud.updatePayment(payment)
        }
        return localResult
    }

    override suspend fun deletePayment(paymentId: String): ApiResponse<Unit> {
        val localResult = local.deletePayment(paymentId)
        if (localResult is ApiResponse.Success && sessionManager.isLoggedIn.value) {
            cloud.deletePayment(paymentId)
        }
        return localResult
    }
}
