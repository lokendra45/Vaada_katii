package com.gaatho.rent.features.auth.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaatho.rent.core.auth.UserRole
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.ui.UiState
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * Stateful container for the Verify OTP code entry screen.
 */
@Composable
fun VerifyOtpScreen(
    phoneNumber: String,
    selectedRole: UserRole,
    onNavigateToHome: () -> Unit,
    onBack: () -> Unit,

) {
    val viewModel: AuthViewModel = koinViewModel()
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is AuthSideEffect.NavigateToHome -> onNavigateToHome()
            is AuthSideEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            is AuthSideEffect.ShowSuccess -> snackbarHostState.showSnackbar(effect.message)
            else -> {}
        }
    }

    VerifyOtpContent(
        phoneNumber = phoneNumber,
        state = state,
        snackbarHostState = snackbarHostState,
        onOtpChanged = { viewModel.onAction(AuthAction.OnOtpChanged(it)) },
        onVerifyClicked = { viewModel.onAction(AuthAction.OnVerifyPhoneOtpClicked(phoneNumber, selectedRole)) },
        onResendClicked = { viewModel.onAction(AuthAction.OnResendOtpClicked(phoneNumber, selectedRole)) },
        onBackClicked = onBack
    )
}

/**
 * Modern rounded box OTP section matching the exact user design spec.
 */
@Composable
fun VerifyOtpContent(
    phoneNumber: String,
    state: AuthState,
    snackbarHostState: SnackbarHostState,
    onOtpChanged: (String) -> Unit,
    onVerifyClicked: () -> Unit,
    onResendClicked: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = state.authUiState is UiState.Loading

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Navigation back button
            IconButton(onClick = onBackClicked, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(16.dp))

            OtpHeaderSection(
                phoneNumber = phoneNumber,
                onBackClicked = onBackClicked
            )

            Spacer(Modifier.height(28.dp))

            OtpInputRow(
                code = state.otpCodeInput,
                onCodeChange = { input ->
                    val digitsOnly = input.filter { it.isDigit() }.take(6)
                    onOtpChanged(digitsOnly)
                    if (digitsOnly.length == 6) {
                        onVerifyClicked()
                    }
                }
            )

            OtpFeedbackSection(state = state)

            Spacer(Modifier.height(24.dp))

            OtpResendSection(
                state = state,
                isLoading = isLoading,
                onResendClicked = onResendClicked
            )

            Spacer(Modifier.weight(1f))

            RentManagerPrimaryButton(
                text = "Verify Code",
                onClick = onVerifyClicked,
                enabled = state.isVerifyEnabled,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun OtpHeaderSection(
    phoneNumber: String,
    onBackClicked: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Text(
        text = "Enter verification code",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = colorScheme.onBackground
    )

    Spacer(Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Sent to $phoneNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit phone number",
            tint = colorScheme.primary,
            modifier = Modifier
                .size(18.dp)
                .clickable { onBackClicked() }
        )
    }
}

@Composable
private fun OtpFeedbackSection(state: AuthState) {
    val colorScheme = MaterialTheme.colorScheme
    val isLoading = state.authUiState is UiState.Loading
    val isError = state.authUiState is UiState.Error

    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "We are validating the OTP",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isError,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = colorScheme.error.copy(alpha = 0.1f),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isError) (state.authUiState as UiState.Error).message else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun OtpResendSection(
    state: AuthState,
    isLoading: Boolean,
    onResendClicked: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!state.canResend) {
            Text(
                text = "Resend code in ",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = formatResendTimer(state.resendTimerSeconds),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = "Didn't receive code?",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Resend",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(enabled = !isLoading) {
                    onResendClicked()
                }
            )
        }
    }
}

@Composable
private fun OtpInputRow(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        value = TextFieldValue(code, selection = TextRange(code.length)),
        onValueChange = {
            if (it.text.length <= 6 && it.text.all { c -> c.isDigit() }) {
                onCodeChange(it.text)
            }
        },
        modifier = modifier.fillMaxWidth().focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        singleLine = true,
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(6) { index ->
                    val char = code.getOrNull(index)?.toString() ?: ""
                    val isFocused = index == code.length
                    OtpBoxCell(
                        char = char,
                        isFocused = isFocused,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        delay(200)
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

@Composable
private fun OtpBoxCell(char: String, isFocused: Boolean, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) colorScheme.primary else colorScheme.outlineVariant,
        label = "BoxBorderColor"
    )

    Surface(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surface,
        border = BorderStroke(if (isFocused) 1.5.dp else 1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (char.isNotEmpty()) {
                Text(
                    text = char,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            } else if (isFocused) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(colorScheme.primary)
                )
            }
        }
    }
}

private fun formatResendTimer(s: Int) =
    "${(s / 60).toString().padStart(2, '0')}:${(s % 60).toString().padStart(2, '0')}"

@Preview
@Composable
private fun VerifyOtpContentDefaultPreview() {
    RentManagerTheme {
        VerifyOtpContent(
            phoneNumber = "+9779841234567",
            state = AuthState(otpCodeInput = "569508", resendTimerSeconds = 45),
            snackbarHostState = remember { SnackbarHostState() },
            onOtpChanged = {},
            onVerifyClicked = {},
            onResendClicked = {},
            onBackClicked = {}
        )
    }
}
