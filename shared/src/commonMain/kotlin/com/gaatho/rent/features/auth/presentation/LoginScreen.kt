package com.gaatho.rent.features.auth.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.auth.UserRole
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.designsystem.components.RentManagerTextField
import com.gaatho.rent.core.designsystem.components.RentManagerSegmentedControl
import com.gaatho.rent.core.designsystem.components.RentManagerSocialButton
import com.gaatho.rent.core.ui.UiState
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit
) {
    val viewModel: AuthViewModel = koinViewModel()
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is AuthSideEffect.NavigateToHome -> onNavigateToHome()
            is AuthSideEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            is AuthSideEffect.ShowSuccess -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    LoginContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@Composable
fun LoginContent(
    state: AuthState,
    snackbarHostState: SnackbarHostState,
    onAction: (AuthAction) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Dark gradient at the very top (using standard colors)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), // Subtle dark gradient
                                MaterialTheme.colorScheme.background.copy(alpha = 0f) // Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 800.dp)
                    .align(Alignment.TopCenter)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Simple representation of a building since we don't have the exact vector
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Home, // Using standard Home icon for now
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = stringResource(Res.string.login_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = stringResource(Res.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Role Segmented Control (Always visible in the design)
                RentManagerSegmentedControl(
                    items = listOf(stringResource(Res.string.role_landlord), stringResource(Res.string.role_tenant)),
                    selectedIndex = if (state.selectedRole == UserRole.LANDLORD) 0 else 1,
                    onItemSelected = { index ->
                        val role = if (index == 0) UserRole.LANDLORD else UserRole.TENANT
                        onAction(AuthAction.OnRoleSelected(role))
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // White Card (Uses Surface color for proper dark mode support)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Google Button
                        RentManagerSocialButton(
                            text = stringResource(Res.string.continue_with_google),
                            onClick = { onAction(AuthAction.OnGoogleAuthClicked) },
                            icon = {
                                Text(
                                    text = stringResource(Res.string.google_g),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                                    color = androidx.compose.ui.graphics.Color(0xFF4285F4), // Keep Google logo colored
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // OR Divider
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = stringResource(Res.string.or_divider),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Email Field
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(Res.string.email_address_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            RentManagerTextField(
                                value = state.emailInput,
                                onValueChange = { onAction(AuthAction.OnEmailChanged(it)) },
                                placeholder = stringResource(Res.string.email_placeholder),
                                enabled = state.authUiState !is UiState.Loading
                            )
                        }

                        // Password Field
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(Res.string.password_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            RentManagerTextField(
                                value = state.passwordInput,
                                onValueChange = { onAction(AuthAction.OnPasswordChanged(it)) },
                                placeholder = stringResource(Res.string.password_placeholder),
                                visualTransformation = PasswordVisualTransformation(),
                                enabled = state.authUiState !is UiState.Loading
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Continue with Email Button
                        RentManagerPrimaryButton(
                            text = if (state.isLoginMode) stringResource(Res.string.continue_with_email) else stringResource(Res.string.sign_up_with_email),
                            onClick = { onAction(AuthAction.OnSubmitEmailAuthClicked) },
                            enabled = state.isSubmitEnabled,
                            isLoading = state.authUiState is UiState.Loading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Forgot Password
                        if (state.isLoginMode) {
                            TextButton(onClick = { /* TODO: Forgot Password */ }) {
                                Text(
                                    text = stringResource(Res.string.forgot_password_question),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Footer Texts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.isLoginMode) stringResource(Res.string.dont_have_account) else stringResource(Res.string.already_have_account),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isLoginMode) stringResource(Res.string.create_account_action) else stringResource(Res.string.sign_in_action),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.clickable { onAction(AuthAction.OnToggleAuthMode) }.padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Continue (Guest) Pill Button
                OutlinedButton(
                    onClick = { onAction(AuthAction.OnGuestAuthClicked) },
                    modifier = Modifier.height(48.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Text(stringResource(Res.string.continue_action), fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Bottom Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(Res.string.privacy_policy), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(Res.string.terms_of_service), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(Res.string.help_center), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun LoginScreenPreview() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        LoginContent(
            state = AuthState(),
            snackbarHostState = SnackbarHostState(),
            onAction = {}
        )
    }
}
