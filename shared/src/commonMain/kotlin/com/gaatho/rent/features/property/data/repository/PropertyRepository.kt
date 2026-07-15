package com.gaatho.rent.features.property.data.repository

import com.gaatho.rent.features.property.data.dto.PropertyDto
import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.mapSuccess
import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

/**
 * Repository for managing Property data.
 *
 * ## Read Path (Store5 + SQLDelight)
 * [getProperties] streams data from the local SQLDelight database (Source of Truth).
 * Store5 automatically fetches from Supabase in the background when needed.
 * The ViewModel observes this stream reactively — no polling required.
 *
 * ## Write Path (Sandwich + Supabase)
 * [createProperty], [updateProperty], [deleteProperty] use Sandwich's [ApiResponse]
 * wrapper for clean, type-safe error handling without try-catch boilerplate.
 *
 * ### Why Sandwich?
 * Sandwich provides a sealed [ApiResponse] (`Success` / `Failure.Error` / `Failure.Exception`)
 * that separates HTTP errors from network exceptions. This enables the ViewModel to
 * handle each case explicitly using Sandwich's DSL:
 * ```kotlin
 * createProperty(property)
 *     .onSuccess { navigateBack() }
 *     .onError { showError(statusCode.code) }
 *     .onException { showError(exception.message) }
 * ```
 *
 * ### Why not return `Result<T>`?
 * Kotlin's `Result<T>` collapses `Error` and `Exception` into one `Failure` case.
 * Sandwich distinguishes HTTP errors (4xx/5xx with body) from network exceptions
 * (no connection, timeout), which is essential for correct user feedback in a
 * mobile app (e.g. "You are offline" vs "Permission denied").
 *
 * @param propertyStore The Store5 instance managing property sync.
 */
class PropertyRepository(
    private val propertyStore: PropertyStore
) {

    // ── Read ─────────────────────────────────────────────────────────────────

    /**
     * Streams properties for a specific owner from the local database.
     * Store5 syncs with Supabase automatically when the data is stale.
     *
     * @param ownerId The authenticated user's ID.
     * @return A cold [Flow] emitting [StoreReadResponse] updates.
     */
    fun getProperties(ownerId: String): Flow<StoreReadResponse<List<Property>>> {
        return propertyStore.store.stream(
            StoreReadRequest.cached(
                key = ownerId,
                refresh = true
            )
        )
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Creates a new property in Supabase.
     *
     * On success, Store5's reactive SQLDelight query automatically emits the
     * new property to any active [getProperties] collectors — no manual refresh.
     *
     * @param property The property domain model to persist.
     * @return [ApiResponse.Success] with Unit on success, or [ApiResponse.Failure] on error.
     */
    suspend fun createProperty(property: Property): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            propertyStore.insert(property.toCreateDto())
        }.mapSuccess { Unit }

    /**
     * Updates an existing property in Supabase.
     *
     * @param property The updated domain model (must have a valid [Property.id]).
     * @return [ApiResponse.Success] with Unit on success, or [ApiResponse.Failure] on error.
     */
    suspend fun updateProperty(property: Property): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            propertyStore.update(property.toCreateDto(), property.id)
        }.mapSuccess { Unit }

    /**
     * Deletes a property from Supabase and removes it from the local cache.
     *
     * @param propertyId The ID of the property to delete.
     * @return [ApiResponse.Success] with Unit on success, or [ApiResponse.Failure] on error.
     */
    suspend fun deleteProperty(propertyId: String): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            propertyStore.delete(propertyId)
        }.mapSuccess { Unit }
}

/**
 * Maps a [Property] domain model to the DTO used for INSERT/UPDATE operations.
 * Does not include `id` or `owner_id` — those are set server-side by Supabase
 * triggers and RLS policies.
 */
private fun Property.toCreateDto() = PropertyDto(
    name = name,
    address = address,
    imageUrl = imageUrl,
    propertyType = propertyType
)
