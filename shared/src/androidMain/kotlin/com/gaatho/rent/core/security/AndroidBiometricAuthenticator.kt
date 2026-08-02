package com.gaatho.rent.core.security

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.gaatho.rent.core.utils.ActivityProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidBiometricAuthenticator : BiometricAuthenticator {

    override fun canAuthenticate(): Boolean {
        val activity = ActivityProvider.activity ?: return false
        val biometricManager = BiometricManager.from(activity)
        // Aligned with authenticate()'s allowed authenticators — otherwise a device
        // with only a PIN/pattern set (no biometric enrolled) would report false here
        // while authenticate() would actually succeed via device credential fallback.
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

        val deferred = CompletableDeferred<BiometricResult>()
        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .setConfirmationRequired(false) // Optimized for passive biometrics
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
                // Single failed attempt (e.g. wrong finger) — not terminal, the
                // system prompt stays open and lets the user retry.
            }
        }

        // FIX: BiometricPrompt.authenticate() calls into FragmentManager internally and
        // must run on the main thread. The caller (Orbit's intent { }) may be on a
        // background dispatcher, so we force this specific call onto Main.immediate.
        withContext(Dispatchers.Main.immediate) {
            val biometricPrompt = BiometricPrompt(activity, executor, callback)

            // If the calling coroutine is cancelled while the dialog is showing
            // (e.g. user navigates away, ViewModel scope dies), dismiss the
            // system prompt instead of leaving it orphaned on screen.
            deferred.invokeOnCompletion { cause ->
                if (cause is CancellationException) {
                    biometricPrompt.cancelAuthentication()
                }
            }

            biometricPrompt.authenticate(promptInfo)
        }

        return deferred.await()
    }
}