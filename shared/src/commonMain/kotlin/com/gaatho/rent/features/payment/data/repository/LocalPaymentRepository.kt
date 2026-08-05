package com.gaatho.rent.features.payment.data.repository

import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.database.dao.PaymentDao
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.gaatho.rent.database.entity.PaymentEntity
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.database.security.SecretString

class LocalPaymentRepository(
    private val paymentDao: PaymentDao
) : PaymentRepository {

    override fun getPaymentsByOwner(ownerId: String): Flow<List<Payment>> {
        return paymentDao.selectPaymentsWithDetailsByOwner(ownerId)
            .map { map ->
                map.map { (payment, _) ->
                    payment.toDomain()
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getPaymentsByTenant(tenantId: String): Flow<List<Payment>> {
        return paymentDao.selectPaymentsWithDetailsByTenant(tenantId)
            .map { map ->
                map.map { (payment, _) ->
                    payment.toDomain()
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getPaymentById(paymentId: String): Flow<Payment?> {
        return paymentDao.selectPaymentWithDetailsById(paymentId)
            .map { map ->
                map.map { (payment, _) ->
                    payment.toDomain()
                }.firstOrNull()
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun createPayment(payment: Payment): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                paymentDao.insertPayment(payment.toEntity())
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e(e) { "Failed to insert payment" }
                ApiResponse.Failure.Exception(e)
            }
        }

    override suspend fun deletePayment(paymentId: String): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                paymentDao.deletePayment(paymentId)
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e(e) { "Failed to delete payment" }
                ApiResponse.Failure.Exception(e)
            }
        }

    override suspend fun updatePayment(payment: Payment): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            try {
                paymentDao.insertPayment(payment.toEntity())
                ApiResponse.Success(Unit)
            } catch (e: Exception) {
                AppLogger.database.e(e) { "Failed to update payment" }
                ApiResponse.Failure.Exception(e)
            }
        }

    private fun PaymentEntity.toDomain() = Payment(
        id = id,
        ownerId = ownerId,
        tenantId = tenantId,
        propertyId = propertyId,
        amount = amount,
        date = date,
        status = status,
        paymentMethod = paymentMethod,
        notes = notes?.value,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Payment.toEntity() = PaymentEntity(
        id = id,
        ownerId = ownerId,
        tenantId = tenantId,
        propertyId = propertyId,
        amount = amount,
        date = date,
        status = status,
        paymentMethod = paymentMethod,
        notes = notes?.let { SecretString(it) },
        createdAt = createdAt ?: DateTimeUtil.nowIsoString(),
        updatedAt = updatedAt ?: DateTimeUtil.nowIsoString()
    )
}
