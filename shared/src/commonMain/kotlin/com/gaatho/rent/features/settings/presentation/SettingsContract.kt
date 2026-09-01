package com.gaatho.rent.features.settings.presentation



data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val emailAlertsEnabled: Boolean = false,
    val biometricsEnabled: Boolean = true,
    val pinLockEnabled: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val languageCode: String? = null,
    val userEmail: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val isPremium: Boolean = false,
    val isLoading: Boolean = false,
    val showLogoutConfirm: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showEditProfileDialog: Boolean = false,
    val isGuest: Boolean = false,
    val avatarUrl: String? = null,
    val isUploadingAvatar: Boolean = false,
    val showAvatarPicker: Boolean = false,
)

sealed interface SettingsSideEffect {
    data object NavigateToLogin : SettingsSideEffect
    data class ShowSnackbar(val message: String) : SettingsSideEffect
}

sealed interface SettingsAction {
    data class OnNotificationsToggled(val enabled: Boolean) : SettingsAction
    data class OnEmailAlertsToggled(val enabled: Boolean) : SettingsAction
    data class OnBiometricsToggled(val enabled: Boolean) : SettingsAction
    data class OnPinLockToggled(val enabled: Boolean) : SettingsAction
    data class OnDarkModeToggled(val enabled: Boolean) : SettingsAction
    data class OnLanguageChanged(val code: String) : SettingsAction
    data object OnUpgradeClicked : SettingsAction
    data object OnSignOutClicked : SettingsAction
    data object OnSignOutConfirmed : SettingsAction
    data object OnSignOutDismissed : SettingsAction
    data object OnDeleteAccountClicked : SettingsAction
    data object OnDeleteAccountConfirmed : SettingsAction
    data object OnDeleteAccountDismissed : SettingsAction
    
    data object OnEditProfileClicked : SettingsAction
    data object OnEditProfileDismissed : SettingsAction
    data class OnSaveProfile(val name: String, val phone: String) : SettingsAction
    data class OnAvatarPicked(val fileName: String, val bytes: ByteArray) : SettingsAction
    data class OnShowAvatarPicker(val show: Boolean) : SettingsAction
}
