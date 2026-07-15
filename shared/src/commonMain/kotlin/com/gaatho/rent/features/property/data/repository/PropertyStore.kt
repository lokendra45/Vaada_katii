package com.gaatho.rent.features.property.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gaatho.rent.database.RentManagerDatabase
import com.gaatho.rent.features.property.data.dto.PropertyDto
import com.gaatho.rent.features.property.data.dto.toDomain
import com.gaatho.rent.features.property.domain.model.Property
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.*
import com.gaatho.rent.database.Property_ as PropertyEntity

/**
 * Store5 implementation for Property data.
 * Manages synchronization between Supabase (Fetcher) and SQLDelight (Source of Truth).
 *
 * Key fixes applied:
 * - Reader now filters by [ownerId] using [selectPropertiesByOwner] (was fetching all tenants' data)
 * - Writer uses safe null guards instead of force-unwrap (!!) to prevent crashes
 *
 * @param supabase The initialized Supabase client.
 * @param database The local SQLDelight database.
 */
class PropertyStore(
    private val supabase: SupabaseClient,
    private val database: RentManagerDatabase
) {
    private val queries = database.rentManagerQueries

    /**
     * The internal Store5 instance for properties.
     * Uses [String] (ownerId) as the key.
     */
    val store: Store<String, List<Property>> = StoreBuilder.from<String, List<PropertyDto>, List<Property>>(
        fetcher = Fetcher.of { ownerId ->
            // Pull only this landlord's properties from Supabase (enforced by RLS too)
            supabase.postgrest["properties"]
                .select {
                    filter {
                        eq("owner_id", ownerId)
                    }
                }
                .decodeList<PropertyDto>()
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { ownerId ->
                // FIX: was selectAllProperties() — now correctly scoped to this owner
                queries.selectPropertiesByOwner(ownerId)
                    .asFlow()
                    .mapToList(Dispatchers.IO)
                    .map { entities ->
                        entities.map { it.toDomain() }
                    }
            },
            writer = { _, dtos ->
                database.transaction {
                    dtos.forEach { dto ->
                        // FIX: safe null guards instead of !! force-unwrap
                        val id = dto.id ?: return@forEach
                        val ownerId = dto.ownerId ?: return@forEach
                        queries.insertProperty(
                            id = id,
                            owner_id = ownerId,
                            name = dto.name,
                            address = dto.address,
                            image_url = dto.imageUrl,
                            property_type = dto.propertyType,
                            created_at = dto.createdAt ?: "",
                            updated_at = dto.updatedAt ?: ""
                        )
                    }
                }
            }
        )
    ).build()

    /**
     * Maps the local SQLDelight entity to the Domain model.
     */
    private fun PropertyEntity.toDomain() = Property(
        id = id,
        ownerId = owner_id,
        name = name,
        address = address,
        imageUrl = image_url,
        propertyType = property_type,
        createdAt = created_at,
        updatedAt = updated_at
    )

    // ── Write Operations ──────────────────────────────────────────────────────

    /**
     * Inserts a new property into Supabase and updates the local SQLDelight cache.
     * The reactive reader in [store] will emit the change automatically.
     *
     * @param dto The DTO to insert (id and owner_id are set by Supabase server-side).
     */
    suspend fun insert(dto: PropertyDto) {
        val inserted = supabase.postgrest["properties"]
            .insert(dto) { select() }
            .decodeSingle<PropertyDto>()

        // Mirror to local cache so reactive SQLDelight query emits immediately
        val id = inserted.id ?: return
        val ownerId = inserted.ownerId ?: return
        database.transaction {
            queries.insertProperty(
                id = id,
                owner_id = ownerId,
                name = inserted.name,
                address = inserted.address,
                image_url = inserted.imageUrl,
                property_type = inserted.propertyType,
                created_at = inserted.createdAt ?: "",
                updated_at = inserted.updatedAt ?: ""
            )
        }
    }

    /**
     * Updates an existing property in Supabase and syncs to the local cache.
     *
     * @param dto The updated DTO fields.
     * @param propertyId The ID of the property to update.
     */
    suspend fun update(dto: PropertyDto, propertyId: String) {
        val updated = supabase.postgrest["properties"]
            .update(dto) {
                filter { eq("id", propertyId) }
                select()
            }
            .decodeSingle<PropertyDto>()

        val id = updated.id ?: return
        val ownerId = updated.ownerId ?: return
        database.transaction {
            queries.insertProperty(
                id = id,
                owner_id = ownerId,
                name = updated.name,
                address = updated.address,
                image_url = updated.imageUrl,
                property_type = updated.propertyType,
                created_at = updated.createdAt ?: "",
                updated_at = updated.updatedAt ?: ""
            )
        }
    }

    /**
     * Deletes a property from Supabase and from the local SQLDelight cache.
     *
     * @param propertyId The ID of the property to delete.
     */
    suspend fun delete(propertyId: String) {
        supabase.postgrest["properties"]
            .delete { filter { eq("id", propertyId) } }

        database.transaction {
            queries.deleteProperty(propertyId)
        }
    }
}
