package com.gaatho.rent.features.settings.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gaatho.rent.core.auth.AuthRepository
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.security.BiometricAuthenticator
import com.gaatho.rent.core.security.BiometricResult
import com.gaatho.rent.core.network.StorageRepository
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import org.orbitmvi.orbit.viewmodel.orbitContainer
import io.github.jan.supabase.auth.auth

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val dataStore: DataStore<Preferences>,
    private val authenticator: BiometricAuthenticator,
    private val supabase: io.github.jan.supabase.SupabaseClient,
    private val storageRepository: StorageRepository
) : MviViewModel<SettingsState, SettingsSideEffect, SettingsAction>() {

    private val KEY_NOTIFICATIONS = booleanPreferencesKey("pref_notifications")
    private val KEY_EMAIL_ALERTS = booleanPreferencesKey("pref_email_alerts")
    private val KEY_BIOMETRICS = booleanPreferencesKey("pref_biometrics")
    private val KEY_PIN_LOCK = booleanPreferencesKey("pref_pin_lock")
    private val KEY_DARK_MODE = booleanPreferencesKey("pref_dark_mode")
    private val KEY_LANGUAGE = stringPreferencesKey("pref_language")

    override val container = orbitContainer<SettingsState, SettingsSideEffect>(
        initialState = SettingsState()
    ) {
        observePreferences()
    }

    private fun observePreferences() = intent {
        // Reactively observe user info
        intent {
            sessionManager.authState.collectLatest { currentAuthState ->
                val user = when (currentAuthState) {
                    is com.gaatho.rent.core.auth.AuthState.Authenticated -> currentAuthState.user
                    is com.gaatho.rent.core.auth.AuthState.Anonymous -> currentAuthState.user
                    else -> null
                }
                
                val email = user?.email ?: ""
                val supabaseUser = supabase.auth.currentUserOrNull()
                val metadataName = supabaseUser?.userMetadata?.get("full_name")?.jsonPrimitive?.content
                val displayName = metadataName ?: user?.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercaseChar() }
                val phone = supabaseUser?.userMetadata?.get("phone")?.jsonPrimitive?.content ?: ""
                val avatarUrl = supabaseUser?.userMetadata?.get("avatar_url")?.jsonPrimitive?.content

                // Only consider as guest if the user is explicitly anonymous
                val isGuest = currentAuthState is com.gaatho.rent.core.auth.AuthState.Anonymous

                reduce {
                    state.copy(
                        userEmail = email,
                        userName = displayName,
                        userPhone = phone,
                        avatarUrl = avatarUrl,
                        isGuest = isGuest,
                    )
                }
            }
        }

        // Reactively observe all preferences
        dataStore.data.collectLatest { prefs ->
            reduce {
                state.copy(
                    notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: true,
                    emailAlertsEnabled = prefs[KEY_EMAIL_ALERTS] ?: false,
                    biometricsEnabled = prefs[KEY_BIOMETRICS] ?: true,
                    pinLockEnabled = prefs[KEY_PIN_LOCK] ?: false,
                    darkModeEnabled = prefs[KEY_DARK_MODE] ?: false,
                    languageCode = prefs[KEY_LANGUAGE]
                )
            }
        }
    }

    override fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnNotificationsToggled -> togglePreference(KEY_NOTIFICATIONS, action.enabled)
            is SettingsAction.OnEmailAlertsToggled -> togglePreference(KEY_EMAIL_ALERTS, action.enabled)
            is SettingsAction.OnBiometricsToggled -> {
                if (action.enabled) {
                    verifyAndEnableBiometrics()
                } else {
                    togglePreference(KEY_BIOMETRICS, false)
                }
            }
            is SettingsAction.OnPinLockToggled -> togglePreference(KEY_PIN_LOCK, action.enabled)
            is SettingsAction.OnDarkModeToggled -> togglePreference(KEY_DARK_MODE, action.enabled)
            is SettingsAction.OnLanguageChanged -> intent {
                dataStore.edit { it[KEY_LANGUAGE] = action.code }
            }

            is SettingsAction.OnUpgradeClicked -> { /* TODO: navigate to paywall */ }

            SettingsAction.OnSignOutClicked ->
                intent { reduce { state.copy(showLogoutConfirm = true) } }
            SettingsAction.OnSignOutDismissed ->
                intent { reduce { state.copy(showLogoutConfirm = false) } }
            SettingsAction.OnSignOutConfirmed -> handleSignOut()

            SettingsAction.OnDeleteAccountClicked ->
                intent { reduce { state.copy(showDeleteConfirm = true) } }
            SettingsAction.OnDeleteAccountDismissed ->
                intent { reduce { state.copy(showDeleteConfirm = false) } }
            SettingsAction.OnDeleteAccountConfirmed -> handleSignOut()
            
            SettingsAction.OnEditProfileClicked -> intent { reduce { state.copy(showEditProfileDialog = true) } }
            SettingsAction.OnEditProfileDismissed -> intent { reduce { state.copy(showEditProfileDialog = false) } }
            is SettingsAction.OnSaveProfile -> handleSaveProfile(action.name, action.phone)
            
            is SettingsAction.OnShowAvatarPicker -> intent { reduce { state.copy(showAvatarPicker = action.show) } }
            is SettingsAction.OnAvatarPicked -> handleAvatarPicked(action.fileName, action.bytes)
        }
    }
    
    private fun handleAvatarPicked(fileName: String, bytes: ByteArray) = intent {
        reduce { state.copy(isUploadingAvatar = true, showAvatarPicker = false) }
        val user = supabase.auth.currentUserOrNull()
        if (user == null) {
            reduce { state.copy(isUploadingAvatar = false) }
            postSideEffect(SettingsSideEffect.ShowSnackbar("User not authenticated"))
            return@intent
        }
        val uploadPath = "avatar_${io.ktor.util.date.getTimeMillis()}_$fileName"
        
        when (val uploadResult = storageRepository.uploadFile("avatars", uploadPath, bytes)) {
            is ApiResponse.Success -> {
                val publicUrl = uploadResult.data
                when (authRepository.updateAvatarUrl(publicUrl)) {
                    is ApiResponse.Success -> {
                        postSideEffect(SettingsSideEffect.ShowSnackbar("Avatar updated successfully"))
                    }
                    else -> postSideEffect(SettingsSideEffect.ShowSnackbar("Failed to update avatar URL"))
                }
            }
            else -> postSideEffect(SettingsSideEffect.ShowSnackbar("Failed to upload avatar image"))
        }
        reduce { state.copy(isUploadingAvatar = false) }
    }

    private fun handleSaveProfile(name: String, phone: String) = intent {
        reduce { state.copy(isLoading = true, showEditProfileDialog = false) }
        val result = authRepository.updateProfile(name, phone)
        reduce { state.copy(isLoading = false) }
        when (result) {
            is ApiResponse.Success -> postSideEffect(SettingsSideEffect.ShowSnackbar("Profile updated successfully"))
            is ApiResponse.Failure.Error, is ApiResponse.Failure.Exception -> postSideEffect(SettingsSideEffect.ShowSnackbar("Failed to update profile"))
        }
    }

    private fun <T> togglePreference(key: Preferences.Key<T>, value: T) = intent {
        dataStore.edit { it[key] = value }
    }

    private fun verifyAndEnableBiometrics() = intent {
        if (!authenticator.canAuthenticate()) {
            postSideEffect(SettingsSideEffect.ShowSnackbar("Biometrics not available or not enrolled on this device."))
            return@intent
        }

        val result = authenticator.authenticate(
            title = "Enable Biometrics",
            subtitle = "Verify your identity to enable fingerprint login"
        )

        when (result) {
            is BiometricResult.Success -> {
                dataStore.edit { it[KEY_BIOMETRICS] = true }
            }
            is BiometricResult.Failure -> {
                postSideEffect(SettingsSideEffect.ShowSnackbar("Authentication failed. Please try again."))
            }
            is BiometricResult.Cancelled -> {
                // Do nothing
            }
            is BiometricResult.NotEnrolled -> {
                postSideEffect(SettingsSideEffect.ShowSnackbar("Biometrics not enrolled. Please set it up in system settings."))
                authenticator.openEnrollmentSettings()
            }
            is BiometricResult.SecurityUpdateRequired -> {
                // Should not happen during initial enablement, but handle for exhaustiveness
                postSideEffect(SettingsSideEffect.ShowSnackbar("Biometric settings changed. Please try again."))
            }
            is BiometricResult.NotAvailable -> {
                postSideEffect(SettingsSideEffect.ShowSnackbar("Biometrics not available."))
            }
        }
    }

    private fun handleSignOut() = intent {
        reduce { state.copy(isLoading = true, showLogoutConfirm = false, showDeleteConfirm = false) }
        try {
            when (authRepository.signOut()) {
                is ApiResponse.Success -> {
                    postSideEffect(SettingsSideEffect.NavigateToLogin)
                }
                is ApiResponse.Failure.Error, is ApiResponse.Failure.Exception -> {
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(SettingsSideEffect.ShowSnackbar("Sign out failed. Please try again."))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.auth.e(e) { "Sign out threw unexpectedly" }
            reduce { state.copy(isLoading = false) }
            postSideEffect(SettingsSideEffect.ShowSnackbar("Sign out failed. Please try again."))
        }
    }
}
