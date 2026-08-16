package com.gaatho.rent.core.security.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaatho.rent.core.security.BiometricAuthenticator
import com.gaatho.rent.core.security.BiometricResult
import com.gaatho.rent.core.auth.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Manages the "App Locked" state and handles biometric authentication requests.
 */
class SecurityViewModel(
    private val dataStore: DataStore<Preferences>,
    private val authenticator: BiometricAuthenticator,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val KEY_BIOMETRICS = booleanPreferencesKey("pref_biometrics")

    private val _isLocked = MutableStateFlow(false)
    val isLocked = _isLocked.asStateFlow()

    private val _isNotEnrolled = MutableStateFlow(false)
    val isNotEnrolled = _isNotEnrolled.asStateFlow()

    private val _authError = MutableSharedFlow<String>()
    val authError = _authError.asSharedFlow()

    init {
        observeLockState()
    }

    private fun observeLockState() {
        dataStore.data
            .map { it[KEY_BIOMETRICS] ?: false }
            .distinctUntilChanged()
            .onEach { enabled ->
                if (enabled) {
                    _isLocked.value = true
                    _isNotEnrolled.value = false
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEnrollClicked() {
        authenticator.openEnrollmentSettings()
    }

    fun authenticate() {
        viewModelScope.launch {
            val result = authenticator.authenticate(
                title = "Authentication Required",
                subtitle = "Use fingerprint to unlock Rent Manager"
            )

            when (result) {
                is BiometricResult.Success -> {
                    _isLocked.value = false
                    _isNotEnrolled.value = false
                }
                is BiometricResult.NotEnrolled -> {
                    _isNotEnrolled.value = true
                }
                is BiometricResult.Failure -> {
                    // result.message is never forwarded raw — use a fixed friendly message.
                    _authError.emit("Authentication failed. Please try again.")
                }
                is BiometricResult.Cancelled -> {
                    // Do nothing
                }
                is BiometricResult.SecurityUpdateRequired -> {
                    _authError.emit("Biometric settings changed. For your security, please sign in again.")
                    authRepository.signOut()
                    _isLocked.value = false // Dismiss gate to allow re-login
                }
                is BiometricResult.NotAvailable -> {
                    _isLocked.value = false
                }
            }
        }
    }
}
