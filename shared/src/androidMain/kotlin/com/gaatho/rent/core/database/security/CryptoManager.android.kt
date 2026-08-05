package com.gaatho.rent.core.database.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import com.gaatho.rent.core.logging.AppLogger
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.ProviderException
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.jvm.JvmInline

/**
 * Column-level encryption backed directly by the Android Keystore system, following
 * Google's current official guidance:
 *  - Jetpack Security (EncryptedSharedPreferences/EncryptedFile) is fully deprecated as of
 *    security-crypto 1.1.0 with no further releases; Google's replacement guidance is to use
 *    the platform Keystore APIs directly (developer.android.com/privacy-and-security/cryptography).
 *  - AES/GCM/NoPadding with a 256-bit key is Google's recommended Cipher choice
 *    (developer.android.com/privacy-and-security/cryptography#cipher).
 *  - Key material never leaves secure hardware (TEE, or StrongBox where available) - the app
 *    process only ever sees ciphertext in and plaintext out, never the raw key
 *    (developer.android.com/privacy-and-security/keystore#ExtractionPrevention).
 *
 * Deliberately NOT using setUserAuthenticationRequired(): this key protects data-at-rest for
 * app-internal columns, not a user-facing secret gated behind biometrics/PIN. Requiring
 * authentication would mean the key (and all previously encrypted rows) becomes permanently
 * unusable the moment the user changes their screen lock or biometric enrollment - a self-inflicted
 * data-loss bug for a rent-management app where fields need to be readable in the background
 * (sync, notifications, widgets) without an unlock prompt. The KeyPermanentlyInvalidatedException
 * handling below is kept purely as defense-in-depth for edge cases (e.g. OEM keystore resets),
 * not because this key is expected to be authentication-bound.
 *
 * Threading: Android Keystore operations are backed by a system process (Keymaster/KeyMint) and
 * can block; Google explicitly warns against calling AndroidKeyStore on the main thread
 * (StrictMode will flag it). For Room, DAO functions are suspendable and typically run on 
 * an I/O dispatcher. TypeConverters run synchronously inside those already-dispatched calls.
 * Therefore, we don't need to manually think about threading here; Room's suspend requirement 
 * correctly keeps these keystore operations off the main thread.
 */
private const val KEY_ALIAS = "rentmanager_column_encryption_key"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val AES_GCM_CIPHER = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH_BITS = 128
private const val GCM_IV_LENGTH_BYTES = 12

/**
 * Versioned prefix for encrypted payloads. Anything without this prefix is treated as
 * plaintext (e.g. legacy unencrypted rows) rather than guessed at via ad-hoc heuristics
 * like "contains a colon".
 */
private const val ENCRYPTED_PREFIX = "ENC1:"

/**
 * All failure modes are surfaced as typed exceptions instead of being swallowed.
 * Callers (DAO/repository layer) decide what to do — retry, block the write,
 * surface a "couldn't read this field" state, etc. CryptoManager never silently
 * returns plaintext-as-if-encrypted or ciphertext-as-if-decrypted.
 */
sealed class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class KeyUnavailable(cause: Throwable) : CryptoException("Encryption key is unavailable", cause)
    class KeyInvalidated(cause: Throwable) : CryptoException("Key was permanently invalidated; a new key was generated and prior encrypted data is unrecoverable", cause)
    class EncryptionFailed(cause: Throwable) : CryptoException("Failed to encrypt data", cause)
    class DecryptionFailed(cause: Throwable) : CryptoException("Failed to decrypt data", cause)
    class MalformedCiphertext(message: String) : CryptoException(message)
}

actual object CryptoManager {
    private val lock = Any()

    private val keyStore: KeyStore by lazy {
        try {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        } catch (e: Exception) {
            AppLogger.database.e(e) { "Failed to load AndroidKeyStore" }
            throw CryptoException.KeyUnavailable(e)
        }
    }

    private fun getOrCreateSecretKey(): SecretKey = synchronized(lock) {
        try {
            (keyStore.getKey(KEY_ALIAS, null) as? SecretKey) ?: generateKey()
        } catch (e: KeyPermanentlyInvalidatedException) {
            AppLogger.database.w(e) {
                "Key permanently invalidated (e.g. biometrics/lock screen changed). " +
                    "Regenerating key - existing encrypted rows will fail to decrypt."
            }
            deleteInvalidatedKeyEntry()
            try {
                generateKey()
            } catch (regenFailure: Exception) {
                throw CryptoException.KeyInvalidated(e)
            }
        } catch (e: GeneralSecurityException) {
            throw CryptoException.KeyUnavailable(e)
        }
    }

    private fun deleteInvalidatedKeyEntry() {
        try {
            keyStore.deleteEntry(KEY_ALIAS)
        } catch (e: KeyStoreException) {
            AppLogger.database.e(e) { "Failed to delete invalidated key entry" }
        }
    }

    private fun generateKey(): SecretKey = synchronized(lock) {
        AppLogger.database.i { "Generating new AndroidKeyStore key for column encryption..." }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                keyGenerator.init(buildKeySpec(strongBox = true))
                val key = keyGenerator.generateKey()
                AppLogger.database.i { "Key generated inside StrongBox hardware." }
                return key
            } catch (e: StrongBoxUnavailableException) {
                AppLogger.database.w { "StrongBox unavailable on this device. Falling back to TEE-backed key." }
            } catch (e: ProviderException) {
                // IMPROVEMENT 1: Safe Fallback for buggy StrongBox implementations
                // Some OEM devices falsely advertise StrongBox or crash internally 
                // throwing a generic ProviderException (which is a RuntimeException).
                AppLogger.database.w(e) { "StrongBox threw ProviderException. Falling back to TEE-backed key." }
            } catch (e: GeneralSecurityException) {
                AppLogger.database.w(e) { "StrongBox key generation failed. Falling back to TEE-backed key." }
            }
        }

        return try {
            keyGenerator.init(buildKeySpec(strongBox = false))
            keyGenerator.generateKey()
        } catch (e: GeneralSecurityException) {
            throw CryptoException.KeyUnavailable(e)
        }
    }

    private fun buildKeySpec(strongBox: Boolean): KeyGenParameterSpec {
        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            // IMPROVEMENT 2: Explicitly require randomized encryption
            // This clearly documents that Keystore generates IVs dynamically, meaning
            // developers don't have to worry about manual IV generation/reuse attacks.
            .setRandomizedEncryptionRequired(true)

        if (strongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setIsStrongBoxBacked(true)
        }
        return builder.build()
    }

    /**
     * @throws CryptoException if the key is unavailable/invalidated or encryption fails.
     */
    actual fun encrypt(data: String): String {
        if (data.isEmpty()) return data

        return try {
            val cipher = Cipher.getInstance(AES_GCM_CIPHER)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

            val iv = cipher.iv
            val ciphertext = cipher.doFinal(data.encodeToByteArray())

            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherBase64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)

            "$ENCRYPTED_PREFIX$ivBase64:$cipherBase64"
        } catch (e: CryptoException) {
            throw e
        } catch (e: Exception) {
            AppLogger.database.e(e) { "Encryption failed" }
            throw CryptoException.EncryptionFailed(e)
        }
    }

    /**
     * @throws CryptoException if the payload is malformed, the key is unavailable, or auth fails.
     */
    actual fun decrypt(data: String): String {
        if (data.isEmpty()) return data
        if (!data.startsWith(ENCRYPTED_PREFIX)) {
            return data
        }

        // IMPROVEMENT 3: Performance optimization
        // Avoid using .split(":", limit = 2) inside decryption which creates array and string
        // object allocations during large database mapping queries causing GC pauses.
        // Instead, index directly into the String.
        val delimiterIndex = data.indexOf(':', startIndex = ENCRYPTED_PREFIX.length)
        if (delimiterIndex == -1) {
            throw CryptoException.MalformedCiphertext("Encrypted payload is missing the IV/ciphertext delimiter")
        }

        return try {
            val ivBase64 = data.substring(ENCRYPTED_PREFIX.length, delimiterIndex)
            val cipherBase64 = data.substring(delimiterIndex + 1)

            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val ciphertext = Base64.decode(cipherBase64, Base64.NO_WRAP)

            if (iv.size != GCM_IV_LENGTH_BYTES) {
                throw CryptoException.MalformedCiphertext("Unexpected GCM IV length: ${iv.size} bytes")
            }

            val cipher = Cipher.getInstance(AES_GCM_CIPHER)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

            cipher.doFinal(ciphertext).decodeToString()
        } catch (e: CryptoException) {
            throw e
        } catch (e: AEADBadTagException) {
            AppLogger.database.e(e) { "Decryption failed - auth tag mismatch (data corrupted or tampered)" }
            throw CryptoException.DecryptionFailed(e)
        } catch (e: IllegalArgumentException) {
            AppLogger.database.e(e) { "Decryption failed - payload is not valid Base64" }
            throw CryptoException.MalformedCiphertext("Payload is not valid Base64: ${e.message}")
        } catch (e: Exception) {
            AppLogger.database.e(e) { "Decryption failed" }
            throw CryptoException.DecryptionFailed(e)
        }
    }
}

/**
 * toString()/equals() on value classes delegate to the wrapped value by default, which means
 * an accidental `"user: $secret"` in a log statement or a naive equals-based diff would leak
 * the real value. toString() is redacted here; if you compare SecretStrings anywhere, do it
 * explicitly via `.value` rather than relying on default equals in logs/analytics.
 */
@JvmInline
actual value class SecretString actual constructor(actual val value: String) {
    override fun toString(): String = "SecretString(***redacted***)"
}
