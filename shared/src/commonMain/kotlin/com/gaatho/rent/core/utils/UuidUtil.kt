package com.gaatho.rent.core.utils

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Common utility for generating RFC 9562 **UUID v7** (and v4) identifiers across all KMP targets.
 *
 * ## Why UUID v7 over v4 (`Uuid.random()`) for Databases?
 * - **Timestamp-Sortable & Monotonic**: The first 48 bits encode the current UNIX millisecond timestamp.
 *   Even if multiple IDs are requested at the exact same millisecond, Kotlin guarantees strict monotonicity
 *   within the application lifetime using the "Fixed Bit-Length Dedicated Counter" method (`RFC-9562 §6.2`).
 * - **Zero B-Tree Index Fragmentation**: Because new UUID v7 keys strictly increase over time (`ORDER BY id ASC`),
 *   SQLDelight (`rentmanager.db`) and Supabase Postgres append new rows sequentially to the end of B-tree index
 *   leaves—eliminating random disk page splits and index bloat.
 *
 * ## Security Note (`Cryptographic vs Database Use`)
 * - **Primary Keys (`generateV7()`)**: Ideal for database primary keys, tenant IDs, and guest session IDs.
 * - **Cryptographic Secrets**: Do **NOT** use UUID v7 for encryption keys, auth refresh tokens, or cryptographic
 *   passphrases because the 48-bit timestamp prefix creates a partially predictable bit pattern (at most 74 bits of entropy).
 *   For cryptographic secrets, always use [java.security.SecureRandom] (on Android/JVM) or `Uuid.random()` (v4).
 */
@OptIn(ExperimentalUuidApi::class)
object UuidUtil {

    /**
     * Generates a new time-based sortable RFC 9562 **UUID v7** instance.
     *
     * - Uses UNIX millisecond timestamp prefix + CSPRNG random suffix with strict application-level monotonicity.
     * - Best suited for relational database primary keys (`Property.id`, `Tenant.id`, `Payment.id`).
     */
    fun generateV7(): Uuid = Uuid.generateV7()

    /**
     * Generates a string representation of an RFC 9562 **UUID v7**
     * (e.g., `0190a2d5-7c3e-783a-8f19-a4b2c1d3e4f5`).
     */
    fun generateV7String(): String = generateV7().toString()

    /**
     * Generates a random RFC 4122 **UUID v4** string (`Uuid.random()`) when pure 122-bit non-chronological
     * randomness is required.
     */
    fun randomV4String(): String = Uuid.random().toString()

    /**
     * Generates a guest session identifier prefixed with `guest_` backed by sequential **UUID v7**.
     */
    fun randomGuestId(): String = "guest_${generateV7String()}"
}
