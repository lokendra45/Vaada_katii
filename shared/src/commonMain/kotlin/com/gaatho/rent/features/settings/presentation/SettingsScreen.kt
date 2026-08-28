package com.gaatho.rent.features.settings.presentation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.environment.LanguageViewModel
import com.gaatho.rent.core.ui.components.AppConfirmDialog
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.about_gharbhada
import rentmanagerapp.shared.generated.resources.account_section
import rentmanagerapp.shared.generated.resources.biometric_lock
import rentmanagerapp.shared.generated.resources.change_password
import rentmanagerapp.shared.generated.resources.contact_us
import rentmanagerapp.shared.generated.resources.currency
import rentmanagerapp.shared.generated.resources.dark_mode
import rentmanagerapp.shared.generated.resources.delete_account_desc
import rentmanagerapp.shared.generated.resources.delete_account_title
import rentmanagerapp.shared.generated.resources.delete_forever_action
import rentmanagerapp.shared.generated.resources.edit_profile
import rentmanagerapp.shared.generated.resources.guest
import rentmanagerapp.shared.generated.resources.help_center
import rentmanagerapp.shared.generated.resources.language
import rentmanagerapp.shared.generated.resources.language_english
import rentmanagerapp.shared.generated.resources.language_nepali
import rentmanagerapp.shared.generated.resources.log_out
import rentmanagerapp.shared.generated.resources.logout_action
import rentmanagerapp.shared.generated.resources.logout_desc
import rentmanagerapp.shared.generated.resources.logout_title
import rentmanagerapp.shared.generated.resources.not_signed_in
import rentmanagerapp.shared.generated.resources.notifications_label
import rentmanagerapp.shared.generated.resources.pin_lock
import rentmanagerapp.shared.generated.resources.preferences_section
import rentmanagerapp.shared.generated.resources.security_section
import rentmanagerapp.shared.generated.resources.sign_in_action
import rentmanagerapp.shared.generated.resources.support_section

@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val languageViewModel = koinViewModel<LanguageViewModel>()
    val currentLanguageCode by languageViewModel.languageCode.collectAsStateWithLifecycle()
    
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is SettingsSideEffect.NavigateToLogin -> onNavigateToLogin()
            is SettingsSideEffect.ShowSnackbar ->
                snackbarHostState.showSnackbar(effect.message)
        }
    }

    if (state.showLogoutConfirm) {
        AppConfirmDialog(
            icon        = Icons.AutoMirrored.Outlined.Logout,
            title       = stringResource(Res.string.logout_title),
            body        = stringResource(Res.string.logout_desc),
            confirmText = stringResource(Res.string.logout_action),
            onConfirm   = { viewModel.onAction(SettingsAction.OnSignOutConfirmed) },
            onDismiss   = { viewModel.onAction(SettingsAction.OnSignOutDismissed) },
            variant     = AppConfirmDialog.Variant.Danger
        )
    }

    if (state.showDeleteConfirm) {
        AppConfirmDialog(
            icon        = Icons.Outlined.DeleteForever,
            title       = stringResource(Res.string.delete_account_title),
            body        = stringResource(Res.string.delete_account_desc),
            confirmText = stringResource(Res.string.delete_forever_action),
            onConfirm   = { viewModel.onAction(SettingsAction.OnDeleteAccountConfirmed) },
            onDismiss   = { viewModel.onAction(SettingsAction.OnDeleteAccountDismissed) },
            variant     = AppConfirmDialog.Variant.Danger
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { padding ->
        SettingsContent(
            state = state,
            currentLanguageCode = currentLanguageCode,
            onAction = viewModel::onAction,
            onLanguageChanged = languageViewModel::switchLanguage,
            onNavigateToLogin = onNavigateToLogin,
            modifier = Modifier.padding(padding)
        )
    }
}

// ─── Scrollable content ────────────────────────────────────────────────────────

@Composable
private fun SettingsContent(
    state: SettingsState,
    currentLanguageCode: String?,
    onAction: (SettingsAction) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
    ) {

        // ── Profile Header ─────────────────────────────
        Spacer(modifier = Modifier.height(16.dp))
        SettingsCard {
            SettingsNavRow(
                leading = {
                    ProfileAvatar(
                        imageUrl = null,
                        displayName = state.userName,
                        modifier = Modifier.size(56.dp)
                    )
                },
                title = state.userName.ifEmpty { stringResource(Res.string.guest) },
                subtitle = state.userEmail.ifEmpty { stringResource(Res.string.not_signed_in) },
                showDivider = false,
                onClick = {}
            )
        }

        // ── Account ─────────────────────────────────────
        SettingsGroup(title = stringResource(Res.string.account_section)) {
            SettingsNavRow(
                leading = { SettingsIcon(icon = Icons.Outlined.Person) },
                title = stringResource(Res.string.edit_profile),
                showDivider = true,
                onClick = {}
            )
            SettingsNavRow(
                leading = { SettingsIcon(icon = Icons.Outlined.Lock) },
                title = stringResource(Res.string.change_password),
                showDivider = false,
                onClick = {}
            )
        }

        // ── Preferences ────────────────────────────────
        SettingsGroup(title = stringResource(Res.string.preferences_section)) {
            SettingsToggleRow(
                icon = Icons.Outlined.Notifications,
                title = stringResource(Res.string.notifications_label),
                checked = state.notificationsEnabled,
                showDivider = true,
                onCheckedChange = { onAction(SettingsAction.OnNotificationsToggled(it)) }
            )
            SettingsToggleRow(
                icon = Icons.Outlined.DarkMode,
                title = stringResource(Res.string.dark_mode),
                checked = state.darkModeEnabled,
                showDivider = true,
                onCheckedChange = { onAction(SettingsAction.OnDarkModeToggled(it)) }
            )
            Box {
                var languageDropDownVisible by remember { mutableStateOf(false) }
                SettingsNavRow(
                    leading = { SettingsIcon(icon = Icons.Outlined.Language) },
                    title = stringResource(Res.string.language),
                    trailingLabel = if (currentLanguageCode == "ne") "Nepali (नेपाली)" else "English",
                    showDivider = true,
                    onClick = { languageDropDownVisible = true }
                )
                DropdownMenu(
                    expanded = languageDropDownVisible,
                    onDismissRequest = { languageDropDownVisible = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.language_english)) },
                        onClick = {
                            onLanguageChanged("en")
                            languageDropDownVisible = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.language_nepali)) },
                        onClick = {
                            onLanguageChanged("ne")
                            languageDropDownVisible = false
                        }
                    )
                }
            }
            SettingsNavRow(
                leading = { SettingsIcon(icon = Icons.Outlined.CurrencyRupee) },
                title = stringResource(Res.string.currency),
                trailingLabel = "NPR (रू)",
                showDivider = false,
                onClick = {}
            )
        }

        // ── Security ───────────────────────────────────
        SettingsGroup(title = stringResource(Res.string.security_section)) {
            SettingsToggleRow(
                icon = Icons.Outlined.Fingerprint,
                title = stringResource(Res.string.biometric_lock),
                checked = state.biometricsEnabled,
                showDivider = true,
                onCheckedChange = { onAction(SettingsAction.OnBiometricsToggled(it)) }
            )
            SettingsToggleRow(
                icon = Icons.Outlined.Key,
                title = stringResource(Res.string.pin_lock),
                checked = state.pinLockEnabled,
                showDivider = false,
                onCheckedChange = { onAction(SettingsAction.OnPinLockToggled(it)) }
            )
        }

        // ── Support ────────────────────────────────────
        SettingsGroup(title = stringResource(Res.string.support_section)) {
            SettingsNavRow(
                leading = { SettingsIcon(icon = Icons.AutoMirrored.Outlined.HelpOutline) },
                title = stringResource(Res.string.help_center),
                showDivider = true,
                onClick = {}
            )
            SettingsNavRow(
                leading = { SettingsIcon(icon = Icons.Outlined.Mail) },
                title = stringResource(Res.string.contact_us),
                showDivider = true,
                onClick = {}
            )
            SettingsNavRow(
                leading = { SettingsIcon(icon = Icons.Outlined.Info) },
                title = stringResource(Res.string.about_gharbhada),
                trailingLabel = "v1.4.0",
                showDivider = true,
                onClick = {}
            )
            if (state.isGuest) {
                SettingsNavRow(
                    leading = { SettingsIcon(icon = Icons.AutoMirrored.Outlined.Login, tint = MaterialTheme.colorScheme.primary) },
                    title = stringResource(Res.string.sign_in_action),
                    titleColor = MaterialTheme.colorScheme.primary,
                    showDivider = false,
                    onClick = onNavigateToLogin
                )
            } else {
                SettingsNavRow(
                    leading = { SettingsIcon(icon = Icons.AutoMirrored.Outlined.Logout, tint = AppColors.Error) },
                    title = stringResource(Res.string.log_out),
                    titleColor = AppColors.Error,
                    showDivider = false,
                    onClick = { onAction(SettingsAction.OnSignOutClicked) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Group wrapper ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 10.dp)
    )
    SettingsCard { content() }
}

// ─── Card wrapper ──────────────────────────────────────────────────────────────

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Column { content() }
    }
}

// ─── Navigation row (chevron right) ───────────────────────────────────────────

@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String? = null,
    showDivider: Boolean = true,
    trailingLabel: String? = null,
    leading: @Composable () -> Unit = {},
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            leading()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
            if (trailingLabel != null) {
                Text(
                    text = trailingLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 58.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}

// ─── Toggle row ───────────────────────────────────────────────────────────────

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    showDivider: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "toggleScale"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsIcon(icon = icon)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .height(24.dp)
                    .scale(scale),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppColors.EmeraldAccent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 58.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}

// ─── Icon ─────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsIcon(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─── Profile avatar ───────────────────────────────────────────────────────────

@Composable
private fun ProfileAvatar(
    imageUrl: String?,
    displayName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                CircleShape
            )
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            com.gaatho.rent.core.ui.components.AppAsyncImage(
                model = imageUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Settings — Stitch", showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun SettingsScreenPreview() {
    RentManagerTheme {
        SettingsContent(
            state = SettingsState(
                userName = "Ramesh ji",
                userEmail = "ramesh.ji@gharbhada.com",
                notificationsEnabled = true,
                emailAlertsEnabled = false,
                biometricsEnabled = true,
                pinLockEnabled = false,
                isPremium = true
            ),
            onAction = {},
            currentLanguageCode = "ne",
            onLanguageChanged = {},
            onNavigateToLogin = {},
        )
    }
}

@Preview(name = "Settings — Logout Dialog", showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun SettingsLogoutPreview() {
    RentManagerTheme {
        SettingsContent(
            state = SettingsState(
                userName = "Ramesh ji",
                userEmail = "ramesh.ji@gharbhada.com",
                showLogoutConfirm = true
            ),
            onAction = {},
            currentLanguageCode = "ne",
            onLanguageChanged = {},
            onNavigateToLogin = {},
        )
    }
}