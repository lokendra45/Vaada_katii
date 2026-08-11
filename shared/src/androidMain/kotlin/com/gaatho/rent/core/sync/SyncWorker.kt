package com.gaatho.rent.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger

import com.gaatho.rent.database.dao.PropertyDao
import com.gaatho.rent.database.dao.TenantDao
import com.gaatho.rent.database.dao.PaymentDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Worker that handles background syncing of offline records to Supabase.
 * Modeled after the "Now in Android" sync architecture.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val propertyDao: PropertyDao by inject()
    private val tenantDao: TenantDao by inject()
    private val paymentDao: PaymentDao by inject()
    private val supabaseClient: SupabaseClient by inject()

    override suspend fun doWork(): Result {
        Logger.d("SyncWorker") { "Starting offline-to-cloud sync..." }
        
        return try {
            val pendingProperties = propertyDao.getPendingProperties()
            val pendingTenants = tenantDao.getPendingTenants()
            val pendingPayments = paymentDao.getPendingPayments()

            if (pendingProperties.isEmpty() && pendingTenants.isEmpty() && pendingPayments.isEmpty()) {
                Logger.d("SyncWorker") { "No pending records to sync." }
                return Result.success()
            }

            val payload = SyncPayload(
                properties = pendingProperties.map { p ->
                    PropertySyncModel(
                        id = p.id,
                        owner_id = p.ownerId,
                        name = p.name,
                        address = p.address.value, // Decrypted via SecretString
                        image_url = p.imageUrl,
                        property_type = p.propertyType,
                        total_units = p.totalUnits,
                        billing_cycle = p.billingCycle,
                        amenities = p.amenities.toList(),
                        created_at = p.createdAt,
                        updated_at = p.updatedAt
                    )
                },
                tenants = pendingTenants.map { t ->
                    TenantSyncModel(
                        id = t.id,
                        owner_id = t.ownerId,
                        property_id = t.propertyId,
                        name = t.name,
                        email = t.email?.value,
                        phone = t.phone?.value,
                        room_number = t.roomNumber,
                        rent_amount = t.rentAmount,
                        status = t.status,
                        created_at = t.createdAt,
                        updated_at = t.updatedAt
                    )
                },
                payments = pendingPayments.map { p ->
                    PaymentSyncModel(
                        id = p.id,
                        owner_id = p.ownerId,
                        tenant_id = p.tenantId,
                        property_id = p.propertyId,
                        amount = p.amount,
                        date = p.date,
                        payment_method = p.paymentMethod,
                        status = p.status,
                        notes = p.notes?.value,
                        created_at = p.createdAt,
                        updated_at = p.updatedAt
                    )
                }
            )

            Logger.d("SyncWorker") { "Pushing payload to Supabase RPC..." }
            
            // Push to Supabase via RPC
            val jsonObject = Json.encodeToJsonElement(payload).jsonObject
            supabaseClient.postgrest.rpc("sync_offline_data", jsonObject)

            // Mark as synced locally
            if (pendingProperties.isNotEmpty()) propertyDao.markPropertiesAsSynced(pendingProperties.map { it.id })
            if (pendingTenants.isNotEmpty()) tenantDao.markTenantsAsSynced(pendingTenants.map { it.id })
            if (pendingPayments.isNotEmpty()) paymentDao.markPaymentsAsSynced(pendingPayments.map { it.id })
            
            Logger.d("SyncWorker") { "Sync completed successfully." }
            Result.success()
        } catch (e: Exception) {
            Logger.e("SyncWorker", e) { "Sync failed." }
            // Retry later if network fails
            Result.retry()
        }
    }
}
