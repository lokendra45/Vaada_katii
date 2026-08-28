package com.gaatho.rent.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.auth.UserRole
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.designsystem.components.RentManagerSegmentedControl
import com.gaatho.rent.core.designsystem.components.RentManagerTextField
import com.gaatho.rent.core.ui.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.already_have_account
import rentmanagerapp.shared.generated.resources.continue_action
import rentmanagerapp.shared.generated.resources.continue_with_email
import rentmanagerapp.shared.generated.resources.continue_with_google
import rentmanagerapp.shared.generated.resources.create_account_action
import rentmanagerapp.shared.generated.resources.dont_have_account
import rentmanagerapp.shared.generated.resources.email_address_label
import rentmanagerapp.shared.generated.resources.email_placeholder
import rentmanagerapp.shared.generated.resources.forgot_password_question
import rentmanagerapp.shared.generated.resources.help_center
import rentmanagerapp.shared.generated.resources.login_subtitle
import rentmanagerapp.shared.generated.resources.login_title
import rentmanagerapp.shared.generated.resources.or_divider
import rentmanagerapp.shared.generated.resources.password_label
import rentmanagerapp.shared.generated.resources.password_placeholder
import rentmanagerapp.shared.generated.resources.privacy_policy
import rentmanagerapp.shared.generated.resources.role_landlord
import rentmanagerapp.shared.generated.resources.role_tenant
import rentmanagerapp.shared.generated.resources.sign_in_action
import rentmanagerapp.shared.generated.resources.sign_up_with_email
import rentmanagerapp.shared.generated.resources.terms_of_service
import com.gaatho.rent.core.auth.AuthState as CoreAuthState

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit
) {
    val viewModel: AuthViewModel = koinViewModel()
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect(lifecycleState = androidx.lifecycle.Lifecycle.State.RESUMED) { effect ->
        when (effect) {
            is AuthSideEffect.NavigateToHome -> onNavigateToHome()
            is AuthSideEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            is AuthSideEffect.ShowSuccess -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    val supabase: SupabaseClient = koinInject()
    val sessionManager: SessionManager = koinInject()
    val authState by sessionManager.authState.collectAsStateWithLifecycle()

    // Navigation is driven reactively by the authoritative authState (single source of truth)
    // rather than by the synchronous return of an auth call. When the session becomes
    // Authenticated (native Google / email) or Anonymous (guest), we go Home.
    LaunchedEffect(authState) {
        if (authState is CoreAuthState.Authenticated || authState is CoreAuthState.Anonymous) {
            // Stamp the role chosen on the login screen for new social sign-ins
            // (Google) that cannot carry metadata at sign-in time. Best-effort.
            viewModel.onAction(AuthAction.OnEnsureRole)
            onNavigateToHome()
        }
    }

    val googleSignIn = supabase.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> viewModel.onAction(AuthAction.OnGoogleSignInSuccess)
                is NativeSignInResult.Error -> viewModel.onAction(AuthAction.OnGoogleSignInError(result.message))
                is NativeSignInResult.ClosedByUser -> {} // Ignore
                is NativeSignInResult.NetworkError -> viewModel.onAction(AuthAction.OnGoogleSignInError("Network error. Please try again."))
            }
        },
        fallback = {
            viewModel.onAction(AuthAction.OnGoogleAuthClicked) // Fallback to browser
        }
    )

    LoginContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onGoogleSignInClick = { googleSignIn.startFlow() }
    )
}

@Composable
fun LoginContent(
    state: AuthState,
    snackbarHostState: SnackbarHostState,
    onAction: (AuthAction) -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                    style = MaterialTheme.typography.titleLarge,
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
                        // High-Fidelity Google Branded Button
                        OutlinedButton(
                            onClick = onGoogleSignInClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = state.authUiState !is UiState.Loading,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp), // Pill shaped as per modern Google UI
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F1F1F) // Google's standard dark grey text
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF747775)) // Google's standard border color
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Google "G" Logo
                                Box(
                                    modifier = Modifier.size(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF4285F4) // Google Blue
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(Res.string.continue_with_google),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

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
                                    style = MaterialTheme.typography.bodyMedium
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
                        style = MaterialTheme.typography.bodyMedium,
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
            onAction = {},
            onGoogleSignInClick = {}
        )
    }
}
