package com.gaatho.rent.features.settings.presentation

import com.gaatho.rent.core.auth.AuthRepository
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.database.RentManagerDatabase
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.viewmodel.orbitContainer

private const val KEY_NOTIFICATIONS = "pref_notifications"
private const val KEY_EMAIL_ALERTS   = "pref_email_alerts"
private const val KEY_BIOMETRICS     = "pref_biometrics"
private const val KEY_DARK_MODE      = "pref_dark_mode"
private const val KEY_LANGUAGE       = "pref_language"

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val database: RentManagerDatabase,
) : MviViewModel<SettingsState, SettingsSideEffect, SettingsAction>() {

    private val queries get() = database.rentManagerQueries

    override val container = orbitContainer<SettingsState, SettingsSideEffect>(
        initialState = SettingsState()
    ) {
        loadPreferences()
    }

    private fun loadPreferences() = intent {
        val notifications = withContext(Dispatchers.IO) {
            queries.selectSetting(KEY_NOTIFICATIONS).executeAsOneOrNull()
        }?.toBooleanStrictOrNull() ?: true

        val emailAlerts = withContext(Dispatchers.IO) {
            queries.selectSetting(KEY_EMAIL_ALERTS).executeAsOneOrNull()
        }?.toBooleanStrictOrNull() ?: false

        val biometrics = withContext(Dispatchers.IO) {
            queries.selectSetting(KEY_BIOMETRICS).executeAsOneOrNull()
        }?.toBooleanStrictOrNull() ?: true

        val darkMode = withContext(Dispatchers.IO) {
            queries.selectSetting(KEY_DARK_MODE).executeAsOneOrNull()
        }?.toBooleanStrictOrNull() ?: false

        val languageCode = withContext(Dispatchers.IO) {
            queries.selectSetting(KEY_LANGUAGE).executeAsOneOrNull()
        }

        val user = sessionManager.currentUser.value
        val email = user?.email ?: ""
        val displayName = user?.displayName
            ?: email.substringBefore("@").replaceFirstChar { it.uppercaseChar() }

        reduce {
            state.copy(
                notificationsEnabled = notifications,
                emailAlertsEnabled   = emailAlerts,
                biometricsEnabled    = biometrics,
                darkModeEnabled      = darkMode,
                languageCode         = languageCode,
                userEmail            = email,
                userName             = displayName,
            )
        }
    }

    override fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnNotificationsToggled -> persistToggle(
                KEY_NOTIFICATIONS, action.enabled
            ) { copy(notificationsEnabled = action.enabled) }

            is SettingsAction.OnEmailAlertsToggled -> persistToggle(
                KEY_EMAIL_ALERTS, action.enabled
            ) { copy(emailAlertsEnabled = action.enabled) }

            is SettingsAction.OnBiometricsToggled -> persistToggle(
                KEY_BIOMETRICS, action.enabled
            ) { copy(biometricsEnabled = action.enabled) }

            is SettingsAction.OnDarkModeToggled -> persistToggle(
                KEY_DARK_MODE, action.enabled
            ) { copy(darkModeEnabled = action.enabled) }

            is SettingsAction.OnLanguageChanged -> intent {
                val newCode = action.code
                reduce { state.copy(languageCode = newCode) }
                withContext(Dispatchers.IO) {
                    queries.upsertSetting(key = KEY_LANGUAGE, settingValue = newCode)
                }
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
        }
    }

    private fun persistToggle(
        key: String,
        value: Boolean,
        stateReducer: SettingsState.() -> SettingsState
    ) = intent {
        reduce { stateReducer(state) }
        withContext(Dispatchers.IO) {
            queries.upsertSetting(key = key, settingValue = value.toString())
        }
    }

    private fun handleSignOut() = intent {
        reduce { state.copy(isLoading = true, showLogoutConfirm = false, showDeleteConfirm = false) }
        when (authRepository.signOut()) {
            is ApiResponse.Success ->
                postSideEffect(SettingsSideEffect.NavigateToLogin)
            is ApiResponse.Failure.Error, is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isLoading = false) }
                postSideEffect(SettingsSideEffect.ShowSnackbar("Sign out failed. Please try again."))
            }
        }
    }
}
