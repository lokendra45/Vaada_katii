package com.gaatho.rent.features.settings.presentation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.components.AppConfirmDialog
import org.koin.compose.koinInject
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.environment.LanguageViewModel

import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*

@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit = {},
    viewModel: SettingsViewModel = koinInject()
) {
    val languageViewModel = koinViewModel<LanguageViewModel>()
    val currentLanguageCode by languageViewModel.languageCode.collectAsStateWithLifecycle()
    
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is SettingsSideEffect.NavigateToLogin -> onNavigateToLogin()
            is SettingsSideEffect.ShowSnackbar ->
                snackbarHostState.showSnackbar(effect.message)
        }
    }

    // ── Logout confirm dialog
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

    // ── Delete account confirm dialog
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
        topBar = {
            // Stitch: back arrow + centered title
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.settings_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        SettingsContent(
            state = state,
            currentLanguageCode = currentLanguageCode,
            onAction = viewModel::onAction,
            onLanguageChanged = languageViewModel::switchLanguage,
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
    ) {

        // ── 1. Account ────────────────────────────────────────────────────────
        SectionLabel(stringResource(Res.string.account_section))
        SettingsCard {
            // Profile row
            SettingsNavRow(
                leading = {
                    ProfileAvatar(
                        imageUrl = null, // TODO: user avatarUrl
                        displayName = state.userName,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = state.userName.ifEmpty { stringResource(Res.string.guest) },
                subtitle = state.userEmail.ifEmpty { stringResource(Res.string.not_signed_in) },
                showDivider = true,
                onClick = {}
            )
            // Subscription row
            SettingsNavRow(
                leading = {
                    IconCircle(icon = Icons.Outlined.Stars, tint = MaterialTheme.colorScheme.primary)
                },
                title = stringResource(Res.string.subscription),
                trailing = {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = if (state.isPremium) stringResource(Res.string.premium) else stringResource(Res.string.free),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                onClick = { onAction(SettingsAction.OnUpgradeClicked) }
            )
        }

        // ── 2. Notifications ──────────────────────────────────────────────────
        SectionLabel(stringResource(Res.string.notifications_section))
        SettingsCard {
            SettingsToggleRow(
                icon = Icons.Outlined.Notifications,
                title = stringResource(Res.string.push_notifications),
                checked = state.notificationsEnabled,
                showDivider = true,
                onCheckedChange = { onAction(SettingsAction.OnNotificationsToggled(it)) }
            )
            SettingsToggleRow(
                icon = Icons.Outlined.Email,
                title = stringResource(Res.string.email_alerts),
                checked = state.emailAlertsEnabled,
                showDivider = false,
                onCheckedChange = { onAction(SettingsAction.OnEmailAlertsToggled(it)) }
            )
        }

        // ── 3. Security ───────────────────────────────────────────────────────
        SectionLabel(stringResource(Res.string.security_section))
        SettingsCard {
            SettingsNavRow(
                leading = { IconCircle(icon = Icons.Outlined.Lock) },
                title = stringResource(Res.string.change_password),
                showDivider = true,
                onClick = {}
            )
            SettingsToggleRow(
                icon = Icons.Outlined.Fingerprint,
                title = stringResource(Res.string.biometrics),
                subtitle = stringResource(Res.string.face_id_touch_id),
                checked = state.biometricsEnabled,
                showDivider = false,
                onCheckedChange = { onAction(SettingsAction.OnBiometricsToggled(it)) }
            )
        }

        // ── 4. Preferences ────────────────────────────────────────────────────
        SectionLabel(stringResource(Res.string.preferences_section))
        SettingsCard {
            SettingsToggleRow(
                icon = Icons.Outlined.DarkMode,
                title = stringResource(Res.string.dark_mode),
                checked = state.darkModeEnabled,
                showDivider = true,
                onCheckedChange = { onAction(SettingsAction.OnDarkModeToggled(it)) }
            )
            SettingsNavRow(
                leading = { IconCircle(icon = Icons.Outlined.CurrencyRupee) },
                title = stringResource(Res.string.currency),
                trailingLabel = "NPR",
                showDivider = true,
                onClick = {}
            )
            Box {
                var languageDropDownVisible by remember { mutableStateOf(false) }
                SettingsNavRow(
                    leading = { IconCircle(icon = Icons.Outlined.Language) },
                    title = stringResource(Res.string.language),
                    trailingLabel = if (currentLanguageCode == "ne") "नेपाली" else "English",
                    showDivider = false,
                    onClick = { 
                        languageDropDownVisible = true
                    }
                )
                DropdownMenu(
                    expanded = languageDropDownVisible,
                    onDismissRequest = { languageDropDownVisible = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("English") },
                        onClick = {
                            onLanguageChanged("en")
                            languageDropDownVisible = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("नेपाली") },
                        onClick = {
                            onLanguageChanged("ne")
                            languageDropDownVisible = false
                        }
                    )
                }
            }
        }

        // ── 5. Help & Support ─────────────────────────────────────────────────
        SectionLabel(stringResource(Res.string.help_support_section))
        SettingsCard {
            SettingsNavRow(
                leading = { IconCircle(icon = Icons.AutoMirrored.Outlined.HelpOutline) },
                title = stringResource(Res.string.faq),
                showDivider = true,
                onClick = {}
            )
            SettingsNavRow(
                leading = { IconCircle(icon = Icons.Outlined.SupportAgent) },
                title = stringResource(Res.string.contact_us),
                showDivider = true,
                onClick = {}
            )
            SettingsNavRow(
                leading = { IconCircle(icon = Icons.Outlined.Policy) },
                title = stringResource(Res.string.privacy_policy),
                showDivider = false,
                onClick = {}
            )
        }

        // ── Logout button ─────────────────────────────────────────────────────
        Spacer(Modifier.height(28.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LogoutButton(onClick = { onAction(SettingsAction.OnSignOutClicked) })
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ─── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .padding(top = 28.dp, bottom = 8.dp)
    )
}

// ─── Card wrapper ──────────────────────────────────────────────────────────────

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
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
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
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
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
            if (trailing != null) {
                trailing()
            } else if (trailingLabel != null) {
                Text(
                    text = trailingLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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
    subtitle: String? = null,
    checked: Boolean,
    showDivider: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    // Spring scale micro-animation on toggle
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconCircle(icon = icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .height(24.dp)
                    .scale(scale),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
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

// ─── Icon circle ──────────────────────────────────────────────────────────────

@Composable
private fun IconCircle(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
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
            AsyncImage(
                model = imageUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ─── Logout button ────────────────────────────────────────────────────────────

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "logoutScale"
    )

    TextButton(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.logout_action),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Settings — Stitch", showBackground = true, backgroundColor = 0xFFFCF8FF)
@Composable
private fun SettingsScreenPreview() {
    RentManagerTheme {
        SettingsContent(
            state = SettingsState(
                userName = "Alex Morgan",
                userEmail = "alex.morgan@example.com",
                notificationsEnabled = true,
                emailAlertsEnabled = false,
                biometricsEnabled = true,
                isPremium = true
            ),
            onAction = {},
            currentLanguageCode = "en",
            onLanguageChanged = {},
        )
    }
}

@Preview(name = "Settings — Logout Dialog", showBackground = true, backgroundColor = 0xFFFCF8FF)
@Composable
private fun SettingsLogoutPreview() {
    RentManagerTheme {
        SettingsContent(
            state = SettingsState(
                userName = "Alex Morgan",
                userEmail = "alex.morgan@example.com",
                showLogoutConfirm = true
            ),
            onAction = {},
            currentLanguageCode = "en",
            onLanguageChanged = {},
        )
    }
}
