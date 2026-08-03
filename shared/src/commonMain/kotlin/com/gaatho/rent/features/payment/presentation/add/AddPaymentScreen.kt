package com.gaatho.rent.features.payment.presentation.add

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppTextField
import kotlinx.collections.immutable.persistentListOf
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

    LaunchedEffect(viewModel.container.sideEffectFlow) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is AddPaymentEffect.NavigateBack -> onNavigateBack()
                is AddPaymentEffect.ShowToast -> {
                    // Show toast (omitted for brevity)
                }
            }
        }
    }

    AddPaymentContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun AddPaymentContent(
    state: AddPaymentState,
    onAction: (AddPaymentAction) -> Unit,
    onNavigateBack: () -> Unit
) {
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
                .imePadding() // Add IME padding here for the whole scrollable content
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.SectionGap))

            // Amount Section
            AmountSection(
                amount = state.amount,
                onAmountChange = { onAction(AddPaymentAction.OnAmountChanged(it)) }
            )

            Spacer(modifier = Modifier.height(Spacing.SectionGap))

            // Details section grouped in a single card
            PaymentDetailsSection(state = state, onAction = onAction)

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
    val isPlaceholder = value.isEmpty()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium
                    ),
                    color = if (isPlaceholder) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                )
            }
            
            if (trailingIcon != null) {
                trailingIcon()
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
private fun AmountSection(amount: String, onAmountChange: (String) -> Unit) {
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
                        withStyle(
                            SpanStyle(
                                fontSize = prefixFontSizeSp.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        ) {
                            append(prefixText)
                        }
                        withStyle(
                            SpanStyle(
                                fontSize = digitFontSizeSp.sp,
                                fontWeight = FontWeight.Normal
                            )
                        ) {
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

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = "Total due: NPR 4,500",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDetailsSection(
    state: AddPaymentState,
    onAction: (AddPaymentAction) -> Unit
) {
    val propertyItems = (state.propertiesState as? UiState.Success)?.data ?: persistentListOf()
    val tenantItems = (state.tenantsState as? UiState.Success)?.data ?: persistentListOf()
    
    var showPropertySheet by remember { mutableStateOf(false) }
    var showTenantSheet by remember { mutableStateOf(false) }

    FormCard {
        FormRow(
            label = stringResource(Res.string.payment_property_label),
            value = propertyItems.find { it.id == state.selectedPropertyId }?.name ?: "",
            placeholder = stringResource(Res.string.payment_property_placeholder),
            leadingIcon = { Icon(Icons.Default.Domain, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)) },
            onClick = { showPropertySheet = true }
        )
        
        FormRow(
            label = stringResource(Res.string.payment_tenant_label),
            value = tenantItems.find { it.id == state.selectedTenantId }?.name ?: "",
            placeholder = stringResource(Res.string.payment_tenant_placeholder),
            leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)) },
            onClick = { showTenantSheet = true }
        )
        
        FormRow(
            label = stringResource(Res.string.payment_date_label),
            value = state.paymentDate,
            placeholder = stringResource(Res.string.payment_date_placeholder),
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)) },
            showDivider = false,
            onClick = { /* Date Picker */ }
        )
    }

    if (showPropertySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPropertySheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.payment_property_label),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 4.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
                propertyItems.forEach { property ->
                    val isSelected = state.selectedPropertyId == property.id
                    Surface(
                        onClick = {
                            onAction(AddPaymentAction.OnPropertySelected(property.id))
                            showPropertySheet = false
                        },
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = property.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTenantSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTenantSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.payment_tenant_label),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 4.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
                tenantItems.forEach { tenant ->
                    val isSelected = state.selectedTenantId == tenant.id
                    Surface(
                        onClick = {
                            onAction(AddPaymentAction.OnTenantSelected(tenant.id))
                            showTenantSheet = false
                        },
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tenant.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodSection(
    selectedMethod: PaymentMethod?,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

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
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
                Text(
                    text = selectedMethod?.let { stringResource(it.labelRes) } ?: "Select method",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = "Change",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select method",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 4.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
                PaymentMethod.entries.forEach { method ->
                    val isSelected = selectedMethod == method
                    Surface(
                        onClick = {
                            onMethodSelected(method)
                            showSheet = false
                        },
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (method) {
                                    PaymentMethod.CASH -> Icons.Filled.Money
                                    PaymentMethod.BANK_TRANSFER -> Icons.Filled.AccountBalance
                                    PaymentMethod.ESEWA -> Icons.Filled.Wallet
                                    PaymentMethod.KHALTI -> Icons.Filled.Payments
                                },
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = stringResource(method.labelRes),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
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
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            )
            BasicTextField(
                value = remarks,
                onValueChange = onRemarksChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                decorationBox = { innerTextField ->
                    if (remarks.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.payment_remarks_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
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
                    style = MaterialTheme.typography.labelMedium.copy(lineHeight = 16.sp),
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
                Text(
                    text = stringResource(Res.string.payment_record_button),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
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
