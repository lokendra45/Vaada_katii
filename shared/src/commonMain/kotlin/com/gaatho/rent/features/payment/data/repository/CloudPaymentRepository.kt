package com.gaatho.rent.features.payment.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.gaatho.rent.core.network.runSupabaseWriteUnit
import com.gaatho.rent.core.network.safeSupabaseRead
import com.gaatho.rent.core.utils.UuidUtil
import com.gaatho.rent.features.payment.data.dto.PaymentDto
import com.gaatho.rent.features.payment.data.dto.toDomain
import com.gaatho.rent.features.payment.data.dto.toDto
import com.gaatho.rent.features.payment.domain.model.Payment
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.gaatho.rent.features.tenant.data.repository.SupabasePagingSource
import com.skydoves.sandwich.ApiResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

import com.gaatho.rent.core.cache.DataStoreCache
import com.gaatho.rent.core.network.safeSupabaseReadWithCache

class CloudPaymentRepository(
    private val supabase: SupabaseClient,
    private val json: Json,
    private val cache: DataStoreCache
) : PaymentRepository {

    override fun getPaymentsByOwner(ownerId: String): Flow<List<Payment>> =
        safeSupabaseReadWithCache(
            default = emptyList(),
            tag = "CloudPaymentRepository.getPaymentsByOwner",
            cache = cache,
            cacheKey = "payments_owner_$ownerId",
            serializer = kotlinx.serialization.builtins.ListSerializer(Payment.serializer())
        ) {
            val dtos = supabase.postgrest[TABLE]
                .select(Columns.raw("*, tenant(name, room_number), property(name)")) {
                    filter {
                        eq("owner_id", ownerId)
                    }
                    order("date", Order.DESCENDING)
                }
                .decodeList<PaymentDto>()
            dtos.map { it.toDomain() }
        }

    override fun getPagedPayments(
        ownerId: String,
        searchQuery: String,
        statusFilter: String
    ): Flow<PagingData<Payment>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = {
            SupabasePagingSource(
                client = supabase,
                table = TABLE,
                json = json,
                serializer = PaymentDto.serializer(),
                orderColumn = "date",
                orderDirection = Order.DESCENDING,
                cursorOf = { it.date },
                idOf = { it.id.orEmpty() }
            ) {
                select(Columns.raw("*, tenant(name, room_number), property(name)")) {
                    filter {
                        eq("owner_id", ownerId)
                        if (searchQuery.isNotBlank()) {
                            ilike("tenant.name", "%$searchQuery%")
                        }
                        if (statusFilter.isNotBlank()) {
                            eq("status", statusFilter)
                        }
                    }
                }
            }
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toDomain() }
    }

    override fun getPaymentsByTenant(tenantId: String): Flow<List<Payment>> =
        safeSupabaseRead(emptyList(), "CloudPaymentRepository.getPaymentsByTenant") {
            val dtos = supabase.postgrest[TABLE]
                .select(Columns.raw("*, tenant(name, room_number), property(name)")) {
                    filter {
                        eq("tenant_id", tenantId)
                    }
                    order("date", Order.DESCENDING)
                }
                .decodeList<PaymentDto>()
            dtos.map { it.toDomain() }
        }

    override fun getPaymentById(paymentId: String): Flow<Payment?> =
        safeSupabaseRead(null, "CloudPaymentRepository.getPaymentById") {
            val dto = supabase.postgrest[TABLE]
                .select(Columns.raw("*, tenant(name, room_number), property(name)")) {
                    filter {
                        eq("id", paymentId)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<PaymentDto>()
            dto?.toDomain()
        }

    override suspend fun createPayment(payment: Payment): ApiResponse<Unit> =
        runSupabaseWriteUnit("CloudPaymentRepository.createPayment") {
            // Idempotency: if the caller didn't provide a key, generate one so the
            // DB unique index (payments_idempotency_key_uidx) can dedupe retries.
            val dto = payment.toDto().let { dto ->
                if (dto.idempotencyKey.isNullOrBlank()) {
                    dto.copy(idempotencyKey = UuidUtil.generateV7String())
                } else dto
            }
            supabase.postgrest[TABLE].insert(dto)
        }

    override suspend fun updatePayment(payment: Payment): ApiResponse<Unit> =
        runSupabaseWriteUnit("CloudPaymentRepository.updatePayment") {
            supabase.postgrest[TABLE].update(payment.toDto()) {
                filter { eq("id", payment.id) }
            }
        }

    override suspend fun deletePayment(paymentId: String): ApiResponse<Unit> =
        runSupabaseWriteUnit("CloudPaymentRepository.deletePayment") {
            supabase.postgrest[TABLE].delete {
                filter { eq("id", paymentId) }
            }
        }

    private companion object {
        const val TABLE = "payments"
        const val PAGE_SIZE = 20
    }
}