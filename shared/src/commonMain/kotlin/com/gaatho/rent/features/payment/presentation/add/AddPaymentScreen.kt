package com.gaatho.rent.features.payment.presentation.add

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.Spacing
import com.gaatho.rent.core.designsystem.interFontFamily
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppDatePickerDialog
import com.gaatho.rent.core.ui.components.AppSelectionBottomSheet
import com.gaatho.rent.core.ui.components.AppSelectionItem
import com.gaatho.rent.core.ui.components.AppSnackbarHost
import com.gaatho.rent.core.ui.components.AppSnackbarVariant
import com.gaatho.rent.core.ui.components.rememberAppSnackbarState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*

@Composable
fun AddPaymentScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: AddPaymentViewModel = koinViewModel()
    val state by viewModel.container.stateFlow.collectAsState()
    val snackbarState = rememberAppSnackbarState()

    LaunchedEffect(viewModel.container.sideEffectFlow) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is AddPaymentEffect.NavigateBack -> onNavigateBack()
                is AddPaymentEffect.ShowSnackbar -> {
                    snackbarState.show(
                        message = effect.message,
                        variant = if (effect.isError) AppSnackbarVariant.ERROR else AppSnackbarVariant.SUCCESS
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AddPaymentContent(
            state = state,
            onAction = viewModel::onAction,
            onNavigateBack = onNavigateBack
        )

        // Floating Snackbar overlay
        AppSnackbarHost(
            state = snackbarState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentContent(
    state: AddPaymentState,
    onAction: (AddPaymentAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val propertyItems = (state.propertiesState as? UiState.Success)?.data ?: persistentListOf()
    val tenantItems = (state.tenantsState as? UiState.Success)?.data ?: persistentListOf()

    var showPropertySheet by remember { mutableStateOf(false) }
    var showTenantSheet by remember { mutableStateOf(false) }

    // Date picker dialog
    if (state.showDatePicker) {
        AppDatePickerDialog(
            selectedDate = state.paymentDate,
            onDateSelected = { onAction(AddPaymentAction.OnPaymentDateChanged(it)) },
            onDismiss = { onAction(AddPaymentAction.OnDatePickerDismissed) },
            title = "Payment Date"
        )
    }

    // Property selection sheet using reusable component
    if (showPropertySheet) {
        AppSelectionBottomSheet(
            title = stringResource(Res.string.payment_property_label),
            items = propertyItems.map { AppSelectionItem(it.id, it.name) },
            selectedId = state.selectedPropertyId,
            onItemSelected = { onAction(AddPaymentAction.OnPropertySelected(it)) },
            onDismiss = { showPropertySheet = false }
        )
    }

    // Tenant selection sheet — shows filtered tenants with room number subtitle
    if (showTenantSheet) {
        AppSelectionBottomSheet(
            title = stringResource(Res.string.payment_tenant_label),
            items = tenantItems.map { t ->
                AppSelectionItem(
                    id = t.id,
                    title = t.name,
                    subtitle = buildString {
                        if (t.roomNumber != null) append("Room ${t.roomNumber}  •  ")
                        append("NPR ${t.rentAmount}")
                    }
                )
            },
            selectedId = state.selectedTenantId,
            onItemSelected = { onAction(AddPaymentAction.OnTenantSelected(it)) },
            onDismiss = { showTenantSheet = false },
            emptyText = if (state.selectedPropertyId == null)
                "Select a property first to see tenants"
            else
                "No active tenants for this property"
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = stringResource(Res.string.payment_add_title),
                onBackClick = onNavigateBack,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        },
        bottomBar = {
            AddPaymentBottomBar(
                enabled = state.canSubmit,
                isSaving = state.isSaving,
                isAgreed = state.isReceiptAgreed,
                onAgreementToggled = { onAction(AddPaymentAction.OnAgreementToggled(it)) },
                onClick = { onAction(AddPaymentAction.OnRecordPaymentClicked) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.SectionGap))

            // Amount Section
            AmountSection(
                amount = state.amount,
                totalDue = state.selectedTenantRentAmount,
                onAmountChange = { onAction(AddPaymentAction.OnAmountChanged(it)) }
            )

            Spacer(modifier = Modifier.height(Spacing.SectionGap))

            // Details card
            PaymentDetailsSection(
                state = state,
                onPropertyRowClick = { showPropertySheet = true },
                onTenantRowClick = { showTenantSheet = true },
                onDateRowClick = { onAction(AddPaymentAction.OnDateFieldClicked) }
            )

            Spacer(modifier = Modifier.height(Spacing.ItemGap))

            PaymentMethodSection(
                selectedMethod = state.selectedPaymentMethod,
                onMethodSelected = { onAction(AddPaymentAction.OnPaymentMethodSelected(it)) }
            )

            Spacer(modifier = Modifier.height(Spacing.ItemGap))

            RemarksSection(
                remarks = state.remarks,
                onRemarksChange = { onAction(AddPaymentAction.OnRemarksChanged(it)) }
            )

            Spacer(modifier = Modifier.height(Spacing.SectionGap))
        }
    }
}

@Composable
fun FormCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            content = content
        )
    }
}

@Composable
fun FormRow(
    label: String,
    value: String,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "row_scale"
    )
    LaunchedEffect(pressed) {
        if (pressed) { delay(120); pressed = false }
    }

    val isPlaceholder = value.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .then(if (onClick != null) Modifier.clickable { pressed = true; onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    leadingIcon()
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
                Text(
                    text = if (isPlaceholder) placeholder else value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium
                    ),
                    color = if (isPlaceholder)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            if (trailingIcon != null) {
                trailingIcon()
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.8.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AmountSection(
    amount: String,
    totalDue: Long?,
    onAmountChange: (String) -> Unit
) {
    var textValue by remember(amount) { mutableStateOf(amount) }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val fontFamily = interFontFamily()

    val maxFontSizeSp = 48f
    val minFontSizeSp = 24f
    val prefixFontSizeSp = 20f
    val prefixText = "NPR "

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val prefixWidthPx = remember {
                textMeasurer.measure(
                    text = prefixText,
                    style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = prefixFontSizeSp.sp)
                ).size.width.toFloat()
            }
            val availableWidthPx = with(density) { (maxWidth * 0.9f).toPx() } - prefixWidthPx
            val displayAmount = textValue.ifEmpty { "0" }

            val digitFontSizeSp = remember(displayAmount, availableWidthPx) {
                var size = maxFontSizeSp
                while (size > minFontSizeSp) {
                    val measured = textMeasurer.measure(
                        text = displayAmount,
                        style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Light, fontSize = size.sp)
                    )
                    if (measured.size.width <= availableWidthPx) break
                    size -= 2f
                }
                size
            }

            val visualTransformation = remember(digitFontSizeSp) {
                VisualTransformation { text ->
                    val original = text.text.ifEmpty { "0" }
                    val transformed = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = prefixFontSizeSp.sp, fontWeight = FontWeight.Medium, color = Color.Gray.copy(alpha = 0.7f))) {
                            append(prefixText)
                        }
                        withStyle(SpanStyle(fontSize = digitFontSizeSp.sp, fontWeight = FontWeight.Normal)) {
                            append(original)
                        }
                    }
                    val prefixLen = prefixText.length
                    val offsetMapping = object : OffsetMapping {
                        override fun originalToTransformed(offset: Int): Int = offset + prefixLen
                        override fun transformedToOriginal(offset: Int): Int =
                            (offset - prefixLen).coerceIn(0, text.text.length)
                    }
                    TransformedText(transformed, offsetMapping)
                }
            }

            BasicTextField(
                value = textValue,
                onValueChange = {
                    val filtered = it.filter { char -> char.isDigit() }
                    textValue = filtered
                    onAmountChange(filtered)
                },
                textStyle = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = digitFontSizeSp.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (totalDue != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "Monthly rent: NPR $totalDue",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentDetailsSection(
    state: AddPaymentState,
    onPropertyRowClick: () -> Unit,
    onTenantRowClick: () -> Unit,
    onDateRowClick: () -> Unit
) {
    val propertyItems = (state.propertiesState as? UiState.Success)?.data ?: persistentListOf()
    val tenantItems = (state.tenantsState as? UiState.Success)?.data ?: persistentListOf()

    FormCard {
        FormRow(
            label = stringResource(Res.string.payment_property_label),
            value = propertyItems.find { it.id == state.selectedPropertyId }?.name ?: "",
            placeholder = stringResource(Res.string.payment_property_placeholder),
            leadingIcon = { Icon(Icons.Default.Domain, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)) },
            onClick = onPropertyRowClick
        )

        FormRow(
            label = stringResource(Res.string.payment_tenant_label),
            value = tenantItems.find { it.id == state.selectedTenantId }?.name ?: "",
            placeholder = stringResource(Res.string.payment_tenant_placeholder),
            leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)) },
            onClick = onTenantRowClick
        )

        FormRow(
            label = stringResource(Res.string.payment_date_label),
            value = state.paymentDate,
            placeholder = stringResource(Res.string.payment_date_placeholder),
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)) },
            showDivider = false,
            onClick = onDateRowClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodSection(
    selectedMethod: PaymentMethod?,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        AppSelectionBottomSheet(
            title = "Payment Method",
            items = PaymentMethod.entries.map { method ->
                AppSelectionItem(
                    id = method,
                    title = method.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() }
                )
            },
            selectedId = selectedMethod,
            onItemSelected = { onMethodSelected(it) },
            onDismiss = { showSheet = false }
        )
    }

    FormCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSheet = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (selectedMethod) {
                PaymentMethod.CASH -> Icons.Filled.Money
                PaymentMethod.BANK_TRANSFER -> Icons.Filled.AccountBalance
                PaymentMethod.ESEWA -> Icons.Filled.Wallet
                PaymentMethod.KHALTI -> Icons.Filled.Payments
                null -> Icons.Filled.Payments
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.payment_method).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
                Text(
                    text = selectedMethod?.let { stringResource(it.labelRes) } ?: "Select method",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Change",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RemarksSection(
    remarks: String,
    onRemarksChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormCard(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = stringResource(Res.string.payment_remarks_label).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            )
            BasicTextField(
                value = remarks,
                onValueChange = onRemarksChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                decorationBox = { innerTextField ->
                    if (remarks.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.payment_remarks_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun AddPaymentBottomBar(
    enabled: Boolean,
    isSaving: Boolean,
    isAgreed: Boolean,
    onAgreementToggled: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { onAgreementToggled(!isAgreed) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = onAgreementToggled,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.payment_info_banner),
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = enabled,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.payment_record_button),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun AddPaymentScreenPreview() {
    RentManagerTheme {
        AddPaymentContent(
            state = AddPaymentState(),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun AddPaymentScreenDarkPreview() {
    RentManagerTheme(darkTheme = true) {
        AddPaymentContent(
            state = AddPaymentState(),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
