package com.gaatho.rent.core.security

/**
 * iOS implementation using LocalAuthentication.
 * Stub for now.
 */
class IosBiometricAuthenticator : BiometricAuthenticator {
    override fun canAuthenticate(): Boolean = false
    override fun openEnrollmentSettings() {}

    override suspend fun authenticate(title: String, subtitle: String): BiometricResult {
        return BiometricResult.NotAvailable
    }
}
