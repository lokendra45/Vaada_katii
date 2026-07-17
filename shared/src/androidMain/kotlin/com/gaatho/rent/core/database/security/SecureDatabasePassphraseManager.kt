package com.gaatho.rent.core.database.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import com.gaatho.rent.core.logging.AppLogger
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.SecureRandom
import java.security.UnrecoverableKeyException
import java.util.Arrays
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val PREFS_FILE_NAME = "rentmanager_secure_db_prefs"
private const val PASSPHRASE_KEY = "sqlcipher_256bit_passphrase_enc"
private const val PASSPHRASE_IV_KEY = "sqlcipher_256bit_passphrase_iv"
private const val PASSPHRASE_LENGTH_BYTES = 32 // 256 bits
private const val KEY_ALIAS = "rentmanager_hardware_db_master_key"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val AES_GCM_CIPHER = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH_BITS = 128

/**
 * Result of retrieving/creating the SQLCipher passphrase.
 *
 * [wasRegenerated] is the important field. It is true in two cases:
 *  - [isFirstRun]: no passphrase existed yet, nothing to worry about.
 *  - NOT first run, but the previous passphrase was confirmed unrecoverable (see
 *    [PassphraseUnavailableException] doc for what does NOT count as "confirmed"), so a brand
 *    new one was generated. In that case any existing SQLCipher database file on disk was
 *    encrypted with a passphrase that no longer exists anywhere and is permanently unreadable.
 *    Callers MUST check for this and delete/recreate the database file before opening it with
 *    [passphrase], or the app will crash on every launch with "file is not a database".
 */
data class PassphraseResult(
    val passphrase: ByteArray,
    val wasRegenerated: Boolean,
    val isFirstRun: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PassphraseResult

        if (wasRegenerated != other.wasRegenerated) return false
        if (isFirstRun != other.isFirstRun) return false
        if (!passphrase.contentEquals(other.passphrase)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = wasRegenerated.hashCode()
        result = 31 * result + isFirstRun.hashCode()
        result = 31 * result + passphrase.contentHashCode()
        return result
    }
}

/**
 * Thrown when the passphrase could not be read or created, and the failure does NOT look like a
 * genuine, confirmed loss of the hardware key -- i.e. it wasn't an [AEADBadTagException]
 * (ciphertext didn't authenticate against the key we have), [UnrecoverableKeyException], or
 * [KeyPermanentlyInvalidatedException]. Those three are handled internally by regenerating a
 * fresh passphrase, because they're the only signals that actually mean "the old key is gone."
 * Everything else -- a transient keystore daemon error, disk full, an I/O hiccup -- is surfaced
 * here instead, so a database that's still perfectly readable never gets silently deleted because
 * of an error we don't understand. Callers should decide whether to retry, show an error, etc.
 */
class PassphraseUnavailableException(message: String, cause: Throwable?) : Exception(message, cause)

/**
 * Manages the Hardware-Bound 256-bit randomized passphrase for SQLCipher database encryption
 * using `KeyGenParameterSpec` + `AndroidKeyStore` + `Cipher`.
 *
 * ## Hardware Security Architecture
 * 1. **Hardware-Bound Secret Key**: Generates a 256-bit AES-GCM key inside `AndroidKeyStore`.
 *    Attempts `setIsStrongBoxBacked(true)` on API 28+ devices (StrongBox does not exist as a
 *    concept before API 28). Falls back to the Trusted Execution Environment (TEE) everywhere
 *    else. The hardware secret key never leaves silicon into system RAM.
 * 2. **Encrypted Passphrase Storage**: The 256-bit SQLCipher passphrase is generated via
 *    [SecureRandom], encrypted by the hardware `SecretKey` using `AES/GCM/NoPadding`, and the
 *    encrypted blob + IV are persisted in private [SharedPreferences] -- durably, via a checked
 *    `commit()`, before it's ever handed back to a caller to key a database with.
 *
 * ## Known limitation, by design
 * `AndroidKeyStore` entries are device- and hardware-bound and are never included in Android
 * backups (cloud or device-transfer), even if the app allows backups. If the SharedPreferences
 * file above ever gets restored onto a device/keystore that doesn't have the matching hardware
 * key, decryption is unrecoverable and this manager will regenerate a new passphrase. Make sure
 * `PREFS_FILE_NAME` and the SQLCipher database file are excluded from backups in your
 * `data_extraction_rules.xml`.
 */
object SecureDatabasePassphraseManager {

    @Synchronized
    fun getOrCreatePassphrase(context: Context): PassphraseResult {
        val prefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val existingEncryptedBase64 = prefs.getString(PASSPHRASE_KEY, null)
        val existingIvBase64 = prefs.getString(PASSPHRASE_IV_KEY, null)
        val isFirstRun = existingEncryptedBase64 == null || existingIvBase64 == null

        if (!isFirstRun) {
            try {
                AppLogger.database.d { "Retrieved existing encrypted hardware-bound SQLCipher passphrase." }
                val encryptedBytes = Base64.decode(existingEncryptedBase64, Base64.NO_WRAP)
                val ivBytes = Base64.decode(existingIvBase64, Base64.NO_WRAP)
                val passphrase = decryptPassphrase(encryptedBytes, ivBytes)
                return PassphraseResult(passphrase, wasRegenerated = false, isFirstRun = false)
            } catch (e: AEADBadTagException) {
                // GCM auth tag didn't verify: either the ciphertext is corrupted, or -- far more
                // likely -- the hardware key we just got back is NOT the one that encrypted it
                // (e.g. getOrCreateHardwareSecretKey() silently rebuilt the key after finding the
                // old entry unrecoverable). Either way the old passphrase, and therefore the
                // existing database, is confirmed gone. Safe to regenerate.
                AppLogger.database.e { "Passphrase ciphertext failed authentication (${e.message}). Regenerating -- existing local database will be unreadable." }
                val passphrase = regenerateOrThrow(context)
                return PassphraseResult(passphrase, wasRegenerated = true, isFirstRun = false)
            } catch (e: UnrecoverableKeyException) {
                AppLogger.database.e { "Hardware key unrecoverable (${e.message}). Regenerating -- existing local database will be unreadable." }
                val passphrase = regenerateOrThrow(context)
                return PassphraseResult(passphrase, wasRegenerated = true, isFirstRun = false)
            } catch (e: KeyPermanentlyInvalidatedException) {
                // Shouldn't normally fire since we set setUserAuthenticationRequired(false), but
                // handled explicitly in case an OEM keystore invalidates it for other reasons.
                AppLogger.database.e { "Hardware key permanently invalidated (${e.message}). Regenerating -- existing local database will be unreadable." }
                val passphrase = regenerateOrThrow(context)
                return PassphraseResult(passphrase, wasRegenerated = true, isFirstRun = false)
            } catch (e: Exception) {
                // Unknown failure -- could be transient (disk I/O, a keystore daemon hiccup, OOM
                // during Base64 decode, etc). Deliberately NOT treated as key loss: destroying the
                // local database on an error we don't understand is worse than surfacing it and
                // letting the caller retry or show a real error.
                AppLogger.database.e { "Unexpected error reading database passphrase (${e.message}). Not regenerating -- treating as recoverable/transient." }
                throw PassphraseUnavailableException("Could not access the secure database passphrase", e)
            }
        }

        AppLogger.database.i { "No existing database passphrase found. Generating secure 256-bit key..." }
        val passphrase = regenerateOrThrow(context)
        return PassphraseResult(passphrase, wasRegenerated = true, isFirstRun = true)
    }

    private fun regenerateOrThrow(context: Context): ByteArray =
        try {
            regenerateAndStorePassphrase(context)
        } catch (e: PassphraseUnavailableException) {
            throw e
        } catch (e: Exception) {
            throw PassphraseUnavailableException("Could not create the secure database passphrase", e)
        }

    private fun regenerateAndStorePassphrase(context: Context): ByteArray {
        val rawPassphrase = ByteArray(PASSPHRASE_LENGTH_BYTES)
        SecureRandom().nextBytes(rawPassphrase)

        val secretKey = getOrCreateHardwareSecretKey()
        val cipher = Cipher.getInstance(AES_GCM_CIPHER)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedBytes = cipher.doFinal(rawPassphrase)
        val ivBytes = cipher.iv

        val prefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        // commit(), not apply(): this write must land on disk *before* the passphrase is used to
        // key a database (see class doc). The boolean result is checked explicitly -- the
        // androidx.core.content.edit { } extension discards it, which would let a disk-full or
        // I/O failure pass silently as if the passphrase had been durably saved.
        val persisted = prefs.edit()
            .putString(PASSPHRASE_KEY, Base64.encodeToString(encryptedBytes, Base64.NO_WRAP))
            .putString(PASSPHRASE_IV_KEY, Base64.encodeToString(ivBytes, Base64.NO_WRAP))
            .commit()

        if (!persisted) {
            AppLogger.database.e { "commit() returned false persisting the new passphrase -- refusing to proceed with an un-persisted key." }
            throw PassphraseUnavailableException("Could not persist the database passphrase to disk", null)
        }

        AppLogger.database.i { "Successfully generated and persisted hardware-protected SQLCipher passphrase." }
        return rawPassphrase
    }

    private fun decryptPassphrase(encryptedBytes: ByteArray, ivBytes: ByteArray): ByteArray {
        val secretKey = getOrCreateHardwareSecretKey()
        val cipher = Cipher.getInstance(AES_GCM_CIPHER)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(encryptedBytes)
    }

    /**
     * Retrieves or generates the hardware AES-256 GCM key inside `AndroidKeyStore`.
     * StrongBox (dedicated secure chip) is attempted first on API 28+ devices; falls back to
     * TEE-backed keys everywhere else, including API 23-27 where StrongBox isn't an option.
     */
    private fun getOrCreateHardwareSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        try {
            val existing = keyStore.getKey(KEY_ALIAS, null)
            if (existing is SecretKey) return existing
        } catch (e: UnrecoverableKeyException) {
            // Corrupted entry -- e.g. after a ROM update or a partially-completed previous
            // key generation. Fall through and rebuild rather than crashing here. Note this
            // means the NEW key will not match old ciphertext -- decryptPassphrase() will
            // correctly surface that as AEADBadTagException, not silently succeed wrong.
            AppLogger.database.w { "Existing hardware key unrecoverable (${e.message}). Rebuilding." }
        } catch (e: KeyStoreException) {
            AppLogger.database.w { "KeyStore read failed (${e.message}). Rebuilding." }
        }

        // On API 28+ (minSdk 28), StrongBox Keymaster APIs are natively available on supported silicon chips.
        val strongBoxEligible = true

        return try {
            generateAndroidKeyStoreKey(useStrongBox = strongBoxEligible).also {
                AppLogger.database.i {
                    if (strongBoxEligible) "Hardware SecretKey generated with StrongBox Keymaster isolation."
                    else "Hardware SecretKey generated inside TEE."
                }
            }
        } catch (e: Exception) {
            // Covers StrongBoxUnavailableException on devices that claim StrongBox support but
            // fail at generation time, plus any other provider-level failure.
            AppLogger.database.w { "StrongBox unsupported or unavailable (${e.message}). Falling back to TEE / KeyStore." }
            try {
                generateAndroidKeyStoreKey(useStrongBox = false).also {
                    AppLogger.database.i { "Hardware SecretKey generated inside Trusted Execution Environment (TEE)." }
                }
            } catch (recoveryEx: Exception) {
                AppLogger.database.e { "KeyStore corrupted or inaccessible (${recoveryEx.message}). Purging alias and recreating..." }
                try {
                    keyStore.deleteEntry(KEY_ALIAS)
                } catch (_: Exception) {
                }
                generateAndroidKeyStoreKey(useStrongBox = false).also {
                    AppLogger.database.i { "Successfully recovered Hardware SecretKey after KeyStore reset." }
                }
            }
        }
    }

    private fun generateAndroidKeyStoreKey(useStrongBox: Boolean): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            // Explicit: DB access must work in the background with no lock-screen prompt.
            .setUserAuthenticationRequired(false)

        if (useStrongBox) {
            builder.setIsStrongBoxBacked(true)
        }

        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }

    /**
     * Overwrites a passphrase byte array with zeros. Only call this at a point where you know no
     * further SQLCipher connections will be opened with it (e.g. logout, driver.close()) -- see
     * the WAL note in DriverFactory for why wiping it too early breaks reopening the database.
     */
    fun wipe(passphrase: ByteArray) {
        Arrays.fill(passphrase, 0)
    }
}