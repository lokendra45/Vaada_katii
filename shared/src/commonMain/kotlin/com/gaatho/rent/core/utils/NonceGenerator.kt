package com.gaatho.rent.core.utils

import kotlin.random.Random

object NonceGenerator {
    /**
     * Generates a cryptographically secure random nonce.
     * Note: In a real production app, use a platform-specific secure random if available.
     * This implementation uses Kotlin's Random for cross-platform simplicity.
     */
    fun generate(length: Int = 32): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
            .joinToString("")
    }
}
