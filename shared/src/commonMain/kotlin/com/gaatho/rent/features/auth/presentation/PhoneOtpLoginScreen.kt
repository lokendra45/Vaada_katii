package com.gaatho.rent.features.auth.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.gaatho.rent.core.auth.UserRole
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * Stateful container for the Phone OTP Login screen.
 */
@Composable
fun PhoneOtpLoginScreen(
    onNavigateToVerifyOtp: (phone: String, role: UserRole) -> Unit,
    onNavigateToHome: () -> Unit
) {
    val viewModel: AuthViewModel = koinViewModel()
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is AuthSideEffect.NavigateToVerifyOtp -> onNavigateToVerifyOtp(effect.phoneNumber, effect.selectedRole)
            is AuthSideEffect.NavigateToHome -> onNavigateToHome()
            is AuthSideEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            is AuthSideEffect.ShowSuccess -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    PhoneOtpLoginContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onPhoneChanged = { viewModel.onAction(AuthAction.OnPhoneChanged(it)) },
        onRoleSelected = { viewModel.onAction(AuthAction.OnRoleSelected(it)) },
        onSendOtpClicked = { viewModel.onAction(AuthAction.OnSendPhoneOtpClicked) }
    )
}

/**
 * Exact 1-to-1 clone of GaathoMobileApp's LoginScreen for Rent Manager Nepal.
 */
@Composable
fun PhoneOtpLoginContent(
    state: AuthState,
    snackbarHostState: SnackbarHostState,
    onPhoneChanged: (String) -> Unit,
    onRoleSelected: (UserRole) -> Unit,
    onSendOtpClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val isLoading = state.authUiState is UiState.Loading
    val isContinueEnabled = state.isContinueEnabled

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            LoginBottomBar(
                isContinueEnabled = isContinueEnabled,
                isLoading = isLoading,
                onSendOtpClicked = onSendOtpClicked
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(32.dp))

                WelcomeHeader()

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Manage your rental properties and tenant payments effortlessly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(32.dp))

                UserTypeToggle(
                    selectedRole = state.selectedRole,
                    onRoleSelected = onRoleSelected
                )

                Spacer(Modifier.height(32.dp))

                MobileInputField(
                    phoneNumberInput = state.phoneNumberInput,
                    isLoading = isLoading,
                    errorMessage = if (state.authUiState is UiState.Error) (state.authUiState as UiState.Error).message else null,
                    onPhoneChanged = onPhoneChanged,
                    focusRequester = focusRequester
                )
            }
        }
    }
}

@Composable
private fun LoginBottomBar(
    isContinueEnabled: Boolean,
    isLoading: Boolean,
    onSendOtpClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RentManagerPrimaryButton(
            text = "Continue",
            onClick = onSendOtpClicked,
            isLoading = isLoading,
            enabled = isContinueEnabled,
            modifier = Modifier.fillMaxWidth().widthIn(max = 440.dp)
        )

        Spacer(Modifier.height(24.dp))

        LegalFooter(modifier = Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun WelcomeHeader() {
    Text(
        text = buildAnnotatedString {
            append("Welcome to ")
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append("Rent Manager")
            }
        },
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun UserTypeToggle(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    val options = persistentListOf("Landlord", "Tenant")
    val selectedOption = if (selectedRole == UserRole.LANDLORD) "Landlord" else "Tenant"

    GaathoSegmentedToggle(
        options = options,
        selectedOption = selectedOption,
        onOptionSelected = { option ->
            onRoleSelected(if (option == "Landlord") UserRole.LANDLORD else UserRole.TENANT)
        }
    )
}

/**
 * Exact replica of GaathoSegmentedToggle from GaathoMobileApp.
 */
@Composable
private fun GaathoSegmentedToggle(
    options: ImmutableList<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = options.indexOf(selectedOption).coerceAtLeast(0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .height(IntrinsicSize.Min)
        ) {
            val animatedIndex by animateFloatAsState(
                targetValue = selectedIndex.toFloat(),
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = 180f
                ),
                label = "ToggleIndicatorOffset"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(1f / options.size.coerceAtLeast(1))
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationX = animatedIndex * size.width
                    }
                    .zIndex(0f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    val animatedTextColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = 180f
                        ),
                        label = "ToggleTextColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOptionSelected(option) }
                            .semantics {
                                role = Role.RadioButton
                                selected = isSelected
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.titleSmall,
                            color = animatedTextColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileInputField(
    phoneNumberInput: String,
    isLoading: Boolean,
    errorMessage: String?,
    onPhoneChanged: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Column {
        Text(
            text = "Mobile Number",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MobileNumberField(
            value = phoneNumberInput,
            onValueChange = onPhoneChanged,
            errorMessage = errorMessage,
            enabled = !isLoading,
            modifier = Modifier.focusRequester(focusRequester)
        )
    }
}

/**
 * Exact replica of MobileNumberField from GaathoMobileApp (GaathoInputs.kt).
 */
@Composable
private fun MobileNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    countryCode: String = "+977",
    maxLength: Int = 10,
    errorMessage: String? = null,
    enabled: Boolean = true,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isError = errorMessage != null

    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newTextFieldValue ->
                val newText = newTextFieldValue.text
                val filteredText = newText.filter { it.isDigit() }.take(maxLength)
                val removedBeforeSelection = newText.substring(0, newTextFieldValue.selection.start)
                    .count { !it.isDigit() }
                val newSelectionStart = (newTextFieldValue.selection.start - removedBeforeSelection)
                    .coerceIn(0, filteredText.length)
                val newSelectionEnd = (newTextFieldValue.selection.end - removedBeforeSelection)
                    .coerceIn(0, filteredText.length)

                val finalTextFieldValue = newTextFieldValue.copy(
                    text = filteredText,
                    selection = TextRange(newSelectionStart, newSelectionEnd)
                )

                textFieldValue = finalTextFieldValue
                onValueChange(filteredText)

                if (filteredText.length == maxLength) {
                    keyboardController?.hide()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Mobile Number"
                    if (isError) error(errorMessage ?: "")
                },
            label = null,
            placeholder = { Text("9801234567") },
            isError = isError,
            enabled = enabled,
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            ),
            leadingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                ) {
                    Text(
                        text = countryCode,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                    )
                    Spacer(Modifier.width(12.dp))
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        thickness = 1.dp,
                        color = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                }
            }
        )

        AnimatedVisibility(
            visible = isError,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Text(
                text = errorMessage.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}


@Composable
private fun LegalFooter(modifier: Modifier = Modifier) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
            append("By continuing you agree to our ")
        }
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
            append("Terms of Service")
        }
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
            append(" and ")
        }
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
            append("Privacy Policy")
        }
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall.copy(
            textAlign = TextAlign.Center
        ),
        modifier = modifier.padding(horizontal = 16.dp),
        onClick = {}
    )
}

@Preview
@Composable
private fun PhoneOtpLoginContentDefaultPreview() {
    RentManagerTheme {
        PhoneOtpLoginContent(
            state = AuthState(
                phoneNumberInput = "9841234567",
                selectedRole = UserRole.LANDLORD
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onPhoneChanged = {},
            onRoleSelected = {},
            onSendOtpClicked = {}
        )
    }
}
