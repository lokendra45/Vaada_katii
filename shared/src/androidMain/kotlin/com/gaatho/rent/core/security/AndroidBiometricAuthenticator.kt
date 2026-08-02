package com.gaatho.rent.core.security

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.gaatho.rent.core.utils.ActivityProvider
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val BIOMETRIC_KEY_ALIAS = "rentmanager_biometric_gate_key"

class AndroidBiometricAuthenticator : BiometricAuthenticator {

    override fun canAuthenticate(): Boolean {
        val activity = ActivityProvider.activity ?: return false
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun openEnrollmentSettings() {
        val activity = ActivityProvider.activity ?: return
        val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
            putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            )
        }
        activity.startActivity(enrollIntent)
    }

    override suspend fun authenticate(title: String, subtitle: String): BiometricResult {
        val activity = ActivityProvider.activity ?: return BiometricResult.Failure("Activity not found")

        val biometricManager = BiometricManager.from(activity)
        val canAuth = biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)

        if (canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            return BiometricResult.NotEnrolled
        } else if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            return BiometricResult.NotAvailable
        }

        // Hardening: Initialize a Cipher with a Biometric-Bound Key
        // This will detect if fingerprints were added/removed since last use.
        val cipherResult = try {
            initCipher()
        } catch (e: KeyPermanentlyInvalidatedException) {
            // CRITICAL: Detection of new fingerprint enrollment!
            return BiometricResult.SecurityUpdateRequired
        } catch (e: Exception) {
            // Transient or hardware error initializing crypto
            null
        }

        val deferred = CompletableDeferred<BiometricResult>()
        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .setConfirmationRequired(false) 
            .build()

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (!deferred.isActive) return
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON ->
                        deferred.complete(BiometricResult.Cancelled)
                    BiometricPrompt.ERROR_LOCKOUT ->
                        deferred.complete(BiometricResult.Failure("Too many attempts. Please try again later."))
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT ->
                        deferred.complete(BiometricResult.Failure("Too many attempts. Biometric sensor is locked."))
                    else ->
                        deferred.complete(BiometricResult.Failure(errString.toString()))
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                if (deferred.isActive) {
                    deferred.complete(BiometricResult.Success)
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }

        withContext(Dispatchers.Main.immediate) {
            val biometricPrompt = BiometricPrompt(activity, executor, callback)

            deferred.invokeOnCompletion { cause ->
                if (cause is CancellationException) {
                    biometricPrompt.cancelAuthentication()
                }
            }

            if (cipherResult != null) {
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipherResult))
            } else {
                // Fallback to basic auth if crypto setup failed for some reason
                biometricPrompt.authenticate(promptInfo)
            }
        }

        return deferred.await()
    }

    private fun initCipher(): Cipher? {
        val secretKey = getOrCreateBiometricKey()
        val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    private fun getOrCreateBiometricKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(BIOMETRIC_KEY_ALIAS, null)
        if (existing is SecretKey) return existing

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(
            BIOMETRIC_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)
            // Hardening: If user registers new biometrics, this key becomes invalid.
            // This prevents a thief from adding their fingerprint and unlocking the app.
            .setInvalidatedByBiometricEnrollment(true)

        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }
}