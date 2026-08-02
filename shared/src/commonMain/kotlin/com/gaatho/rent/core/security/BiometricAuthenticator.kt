package com.gaatho.rent.core.security

/**
 * Clean Architecture interface for biometric authentication.
 * Implementation is platform-specific (Android/iOS).
 */
interface BiometricAuthenticator {
    /**
     * Checks if the device has biometric hardware and enrolled credentials.
     */
    fun canAuthenticate(): Boolean

    /**
     * Opens the system biometric enrollment settings.
     */
    fun openEnrollmentSettings()

    /**
     * Triggers the system biometric prompt.
     * @param title The title of the biometric dialog.
     * @param subtitle The subtitle/description of the biometric dialog.
     * @return [BiometricResult] indicating the outcome.
     */
    suspend fun authenticate(title: String, subtitle: String): BiometricResult
}

sealed class BiometricResult {
    object Success : BiometricResult()
    data class Failure(val message: String) : BiometricResult()
    object Cancelled : BiometricResult()
    object NotAvailable : BiometricResult()
    object NotEnrolled : BiometricResult()
}
