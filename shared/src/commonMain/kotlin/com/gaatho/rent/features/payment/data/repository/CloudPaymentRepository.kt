package com.gaatho.rent.features.payment.data.repository

import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.skydoves.sandwich.ApiResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

class CloudPaymentRepository(
    private val supabase: SupabaseClient
) : PaymentRepository {
    
    private val table = supabase.postgrest["payments"]

    override fun getPaymentsByOwner(ownerId: String): Flow<List<Payment>> = flow {
        // Implement when Supabase sync is fully built.
        // For now, this is a stub.
        emit(emptyList())
    }

    override fun getPaymentsByTenant(tenantId: String): Flow<List<Payment>> {
        return emptyFlow()
    }

    override fun getPaymentById(paymentId: String): Flow<Payment?> {
        return emptyFlow()
    }

    override suspend fun createPayment(payment: Payment): ApiResponse<Unit> {
        return try {
            table.insert(payment)
            ApiResponse.Success(Unit)
        } catch (e: Exception) {
            AppLogger.network.e(e) { "Cloud createPayment failed" }
            ApiResponse.Failure.Exception(e)
        }
    }

    override suspend fun updatePayment(payment: Payment): ApiResponse<Unit> {
        return try {
            table.update(payment) { filter { eq("id", payment.id) } }
            ApiResponse.Success(Unit)
        } catch (e: Exception) {
            AppLogger.network.e(e) { "Cloud updatePayment failed" }
            ApiResponse.Failure.Exception(e)
        }
    }

    override suspend fun deletePayment(paymentId: String): ApiResponse<Unit> {
        return try {
            table.delete { filter { eq("id", paymentId) } }
            ApiResponse.Success(Unit)
        } catch (e: Exception) {
            AppLogger.network.e(e) { "Cloud deletePayment failed" }
            ApiResponse.Failure.Exception(e)
        }
    }
}
