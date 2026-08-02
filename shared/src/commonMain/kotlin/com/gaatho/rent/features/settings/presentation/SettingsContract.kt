package com.gaatho.rent.features.settings.presentation

import androidx.compose.runtime.Immutable


data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val emailAlertsEnabled: Boolean = false,
    val biometricsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val languageCode: String? = null,
    val userEmail: String = "",
    val userName: String = "",
    val isPremium: Boolean = false,
    val isLoading: Boolean = false,
    val showLogoutConfirm: Boolean = false,
    val showDeleteConfirm: Boolean = false,
)

sealed interface SettingsSideEffect {
    data object NavigateToLogin : SettingsSideEffect
    data class ShowSnackbar(val message: String) : SettingsSideEffect
}

sealed interface SettingsAction {
    data class OnNotificationsToggled(val enabled: Boolean) : SettingsAction
    data class OnEmailAlertsToggled(val enabled: Boolean) : SettingsAction
    data class OnBiometricsToggled(val enabled: Boolean) : SettingsAction
    data class OnDarkModeToggled(val enabled: Boolean) : SettingsAction
    data class OnLanguageChanged(val code: String) : SettingsAction
    data object OnUpgradeClicked : SettingsAction
    data object OnSignOutClicked : SettingsAction
    data object OnSignOutConfirmed : SettingsAction
    data object OnSignOutDismissed : SettingsAction
    data object OnDeleteAccountClicked : SettingsAction
    data object OnDeleteAccountConfirmed : SettingsAction
    data object OnDeleteAccountDismissed : SettingsAction
}
