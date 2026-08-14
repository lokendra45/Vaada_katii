package com.gaatho.rent.features.payment.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.gaatho.rent.core.database.security.SecretString
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.utils.IsoDateUtil
import com.gaatho.rent.core.utils.StringUtil
import com.gaatho.rent.database.AppDatabase
import com.gaatho.rent.database.dao.PaymentDao
import com.gaatho.rent.database.entity.PaymentEntity
import com.gaatho.rent.database.projection.PaymentListRow
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalPaymentRepository(
    private val database: AppDatabase,
    private val paymentDao: PaymentDao
) : PaymentRepository {

    override fun getPaymentsByOwner(ownerId: String): Flow<List<Payment>> {
        return paymentDao.selectPaymentsWithDetailsByOwner(ownerId)
            .map { map -> 
                map.keys.filter { it.deletedAt == null }.map { it.toDomain() } 
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getPagedPayments(
        ownerId: String,
        searchQuery: String,
        statusFilter: String
    ): Flow<PagingData<Payment>> {
        val normalizedSearch = StringUtil.escapeLike(StringUtil.normalizeSearch(searchQuery))

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = true,
                maxSize = 100
            ),
            pagingSourceFactory = {
                paymentDao.selectPagedPaymentListRows(
                    ownerId = ownerId,
                    searchQuery = normalizedSearch,
                    statusFilter = statusFilter
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { row -> row.toDomain() }
        }
    }

    override fun getPaymentsByTenant(tenantId: String): Flow<List<Payment>> {
        return paymentDao.selectPaymentsWithDetailsByTenant(tenantId)
            .map { map -> map.map { (payment, _) -> payment.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getPaymentById(paymentId: String): Flow<Payment?> {
        return paymentDao.selectPaymentWithDetailsById(paymentId)
            .map { map -> map.map { (payment, _) -> payment.toDomain() }.firstOrNull() }
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

    // ── Mappers ──────────────────────────────────────────────────────────────

    /**
     * Maps the narrow [PaymentListRow] projection to the [Payment] domain model.
     * tenantName, propertyName, roomNumber come from SQL JOINs.
     */
    private fun PaymentListRow.toDomain() = Payment(
        id = id,
        ownerId = ownerId,
        tenantId = tenantId ?: "",
        propertyId = propertyId,
        amount = amount,
        date = date,
        status = status,
        paymentMethod = paymentMethod,
        tenantName = tenantName,
        propertyName = propertyName,
        roomNumber = tenantRoomNumber
    )

    /** Maps the full [PaymentEntity] (used by detail screens). */
    private fun PaymentEntity.toDomain() = Payment(
        id = id,
        ownerId = ownerId,
        tenantId = tenantId ?: "",
        propertyId = propertyId,
        amount = amount,
        date = date,
        status = status,
        paymentMethod = paymentMethod,
        notes = notes?.value,
        idempotencyKey = idempotencyKey,
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
        idempotencyKey = idempotencyKey,
        createdAt = IsoDateUtil.normalize(createdAt ?: DateTimeUtil.nowIsoString()) ?: DateTimeUtil.nowIsoString(),
        updatedAt = IsoDateUtil.normalize(updatedAt ?: DateTimeUtil.nowIsoString()) ?: DateTimeUtil.nowIsoString(),
        syncStatus = "PENDING"
    )
}
