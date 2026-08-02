package com.gaatho.rent.features.payment.presentation.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.Spacing
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
        onAction = viewModel::onAction
    )
}

@Composable
fun AddPaymentContent(
    state: AddPaymentState,
    onAction: (AddPaymentAction) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            AddPaymentBottomBar(
                enabled = state.canSubmit,
                onClick = { onAction(AddPaymentAction.OnRecordPaymentClicked) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.SectionGap)
        ) {
            Spacer(modifier = Modifier.height(Spacing.ItemGap))
            
            // Custom Back button & Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { onAction(AddPaymentAction.OnBackClicked) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.shapes.medium)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(Res.string.payment_add_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AmountSection(
                amount = state.amount,
                onAmountChange = { onAction(AddPaymentAction.OnAmountChanged(it)) }
            )

            PaymentDetailsSection(state = state, onAction = onAction)
            
            PaymentMethodSection(
                selectedMethod = state.selectedPaymentMethod,
                onMethodSelected = { onAction(AddPaymentAction.OnPaymentMethodSelected(it)) }
            )

            RemarksSection(
                remarks = state.remarks,
                onRemarksChange = { onAction(AddPaymentAction.OnRemarksChanged(it)) }
            )
            
            // Info Banner
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.ItemGap)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(Res.string.payment_info_banner),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.SectionGap))
        }
    }
}


@Composable
private fun AmountSection(amount: String, onAmountChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.ItemGap),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(Res.string.payment_currency),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            var textValue by remember(amount) { mutableStateOf(amount) }
            
            BasicTextField(
                value = textValue,
                onValueChange = { 
                    val filtered = it.filter { char -> char.isDigit() }
                    textValue = filtered
                    onAmountChange(filtered) 
                },
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (textValue.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.payment_amount_placeholder),
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.width(IntrinsicSize.Min).widthIn(min = 40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Text(
                text = stringResource(Res.string.payment_total_due, "4,500"),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun PaymentDetailsSection(
    state: AddPaymentState,
    onAction: (AddPaymentAction) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.ItemGap)) {
        // Property Dropdown
        val propertyItems = (state.propertiesState as? UiState.Success)?.data ?: persistentListOf()
        AppDropdown(
            label = stringResource(Res.string.payment_property_label),
            placeholder = stringResource(Res.string.payment_property_placeholder),
            options = propertyItems,
            selectedItem = propertyItems.find { it.id == state.selectedPropertyId },
            onItemSelected = { onAction(AddPaymentAction.OnPropertySelected(it.id)) },
            itemLabel = { it.name },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Domain,
                    contentDescription = null
                )
            }
        )

        // Tenant Dropdown
        val tenantItems = (state.tenantsState as? UiState.Success)?.data ?: persistentListOf()
        AppDropdown(
            label = stringResource(Res.string.payment_tenant_label),
            placeholder = stringResource(Res.string.payment_tenant_placeholder),
            options = tenantItems,
            selectedItem = tenantItems.find { it.id == state.selectedTenantId },
            onItemSelected = { onAction(AddPaymentAction.OnTenantSelected(it.id)) },
            itemLabel = { it.name },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null
                )
            }
        )

        // Date 
        AppTextField(
            value = state.paymentDate,
            onValueChange = { onAction(AddPaymentAction.OnPaymentDateChanged(it)) },
            label = stringResource(Res.string.payment_date_label),
            placeholder = stringResource(Res.string.payment_date_placeholder),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.ItemGap)) {
        Text(
            text = stringResource(Res.string.payment_method).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Surface(
            onClick = { showSheet = true },
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (selectedMethod) {
                    PaymentMethod.CASH -> Icons.Filled.Money
                    PaymentMethod.BANK_TRANSFER -> Icons.Filled.AccountBalance
                    PaymentMethod.ESEWA -> Icons.Filled.Wallet
                    PaymentMethod.KHALTI -> Icons.Filled.Payments
                    null -> Icons.Filled.Payments
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedMethod?.let { stringResource(it.labelRes) } ?: stringResource(Res.string.payment_method),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.ScreenPadding)
                    .padding(bottom = Spacing.SectionGap),
                verticalArrangement = Arrangement.spacedBy(Spacing.ItemGap)
            ) {
                Text(
                    text = stringResource(Res.string.payment_method),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = Spacing.ItemGap)
                )
                PaymentMethod.entries.forEach { method ->
                    val isSelected = selectedMethod == method
                    val icon = when (method) {
                        PaymentMethod.CASH -> Icons.Filled.Money
                        PaymentMethod.BANK_TRANSFER -> Icons.Filled.AccountBalance
                        PaymentMethod.ESEWA -> Icons.Filled.Wallet
                        PaymentMethod.KHALTI -> Icons.Filled.Payments
                    }
                    Surface(
                        onClick = {
                            onMethodSelected(method)
                            showSheet = false
                        },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().height(64.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = stringResource(method.labelRes),
                                style = MaterialTheme.typography.titleMedium,
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
private fun RemarksSection(remarks: String, onRemarksChange: (String) -> Unit) {
    AppTextField(
        value = remarks,
        onValueChange = onRemarksChange,
        label = stringResource(Res.string.payment_remarks_label),
        placeholder = stringResource(Res.string.payment_remarks_placeholder),
        singleLine = false,
        modifier = Modifier.height(100.dp)
    )
}

@Composable
private fun AddPaymentBottomBar(enabled: Boolean, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenPadding, vertical = Spacing.ItemGap)
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
                enabled = enabled,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.payment_record_button),
                    style = MaterialTheme.typography.titleMedium
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
            onAction = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun AddPaymentScreenDarkPreview() {
    RentManagerTheme(darkTheme = true) {
        AddPaymentContent(
            state = AddPaymentState(),
            onAction = {}
        )
    }
}

