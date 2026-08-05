package com.gaatho.rent.core.database.security

import kotlin.jvm.JvmInline

expect object CryptoManager {
    fun encrypt(data: String): String
    fun decrypt(data: String): String
}

/**
 * A type-safe wrapper for strings that should be encrypted in the database.
 * Room TypeConverters will map this to a ciphertext String.
 */
@JvmInline
expect value class SecretString(val value: String)
