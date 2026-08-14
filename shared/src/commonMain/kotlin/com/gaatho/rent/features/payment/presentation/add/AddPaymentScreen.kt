package com.gaatho.rent.features.payment.presentation.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppDatePickerDialog
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppSnackbarHost
import com.gaatho.rent.core.ui.components.AppSnackbarVariant
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.core.ui.components.rememberAppSnackbarState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.payment_add_title
import rentmanagerapp.shared.generated.resources.payment_amount_label
import rentmanagerapp.shared.generated.resources.payment_amount_placeholder
import rentmanagerapp.shared.generated.resources.payment_currency
import rentmanagerapp.shared.generated.resources.payment_date_label
import rentmanagerapp.shared.generated.resources.payment_date_placeholder
import rentmanagerapp.shared.generated.resources.payment_method
import rentmanagerapp.shared.generated.resources.payment_method_bank
import rentmanagerapp.shared.generated.resources.payment_method_cash
import rentmanagerapp.shared.generated.resources.payment_method_esewa
import rentmanagerapp.shared.generated.resources.payment_method_khalti
import rentmanagerapp.shared.generated.resources.payment_property_unit_label
import rentmanagerapp.shared.generated.resources.payment_property_unit_placeholder
import rentmanagerapp.shared.generated.resources.payment_record_button
import rentmanagerapp.shared.generated.resources.payment_remarks_label
import rentmanagerapp.shared.generated.resources.payment_remarks_placeholder
import rentmanagerapp.shared.generated.resources.payment_tenant_label
import rentmanagerapp.shared.generated.resources.payment_tenant_placeholder

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

    val labelStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
    val fieldStyle = MaterialTheme.typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    )
    val fieldShape = RoundedCornerShape(10.dp)

    val selectedTenant = tenantItems.find { it.id == state.selectedTenantId }
    val propertyName = propertyItems.find { it.id == state.selectedPropertyId }?.name
    val propertyUnitLabel = when {
        selectedTenant != null && propertyName != null ->
            if (selectedTenant.roomNumber != null) "$propertyName - Unit ${selectedTenant.roomNumber}" else propertyName

        else -> ""
    }
    val dateDisplay = formatDisplayDate(state.paymentDate)

    if (state.showDatePicker) {
        AppDatePickerDialog(
            selectedDate = state.paymentDate,
            onDateSelected = { onAction(AddPaymentAction.OnPaymentDateChanged(it)) },
            onDismiss = { onAction(AddPaymentAction.OnDatePickerDismissed) },
            title = "Payment Date"
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.payment_add_title),
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            RecordPaymentBottomBar(
                enabled = state.canSubmit,
                isSaving = state.isSaving,
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Select Tenant
            AppDropdown(
                options = tenantItems,
                selectedItem = tenantItems.find { it.id == state.selectedTenantId },
                itemLabel = { it.name },
                onItemSelected = { onAction(AddPaymentAction.OnTenantSelected(it.id)) },
                label = stringResource(Res.string.payment_tenant_label),
                placeholder = stringResource(Res.string.payment_tenant_placeholder),
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Property & Unit (derived from tenant, disabled)
            DisabledField(
                text = propertyUnitLabel,
                placeholder = stringResource(Res.string.payment_property_unit_placeholder),
                label = stringResource(Res.string.payment_property_unit_label),
                labelStyle = labelStyle,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Amount (NPR)
            AppTextField(
                value = state.amount,
                onValueChange = { onAction(AddPaymentAction.OnAmountChanged(it)) },
                label = stringResource(Res.string.payment_amount_label),
                placeholder = stringResource(Res.string.payment_amount_placeholder),
                prefix = stringResource(Res.string.payment_currency).trimEnd(),
                prefixColor = AppColors.EmeraldAccent,
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle.copy(fontWeight = FontWeight.Medium),
                shape = fieldShape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Payment Method
            Column {
                Text(
                    text = stringResource(Res.string.payment_method),
                    style = labelStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                PaymentMethodChips(
                    selectedMethod = state.selectedPaymentMethod,
                    onMethodSelected = { onAction(AddPaymentAction.OnPaymentMethodSelected(it)) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Received Date
            AppTextField(
                value = dateDisplay,
                onValueChange = {},
                readOnly = true,
                label = stringResource(Res.string.payment_date_label),
                placeholder = stringResource(Res.string.payment_date_placeholder),
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = { onAction(AddPaymentAction.OnDateFieldClicked) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Note (Optional)
            AppTextField(
                value = state.remarks,
                onValueChange = { onAction(AddPaymentAction.OnRemarksChanged(it)) },
                label = stringResource(Res.string.payment_remarks_label),
                placeholder = stringResource(Res.string.payment_remarks_placeholder),
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DisabledField(
    text: String,
    placeholder: String,
    label: String,
    labelStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = labelStyle,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (text.isEmpty()) placeholder else text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodChips(
    selectedMethod: PaymentMethod?,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    val methods = listOf(
        PaymentMethod.CASH,
        PaymentMethod.ESEWA,
        PaymentMethod.KHALTI,
        PaymentMethod.BANK_TRANSFER
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        methods.forEach { method ->
            val isSelected = selectedMethod == method
            val label = when (method) {
                PaymentMethod.CASH -> stringResource(Res.string.payment_method_cash)
                PaymentMethod.ESEWA -> stringResource(Res.string.payment_method_esewa)
                PaymentMethod.KHALTI -> stringResource(Res.string.payment_method_khalti)
                PaymentMethod.BANK_TRANSFER -> stringResource(Res.string.payment_method_bank)
            }
            Surface(
                onClick = { onMethodSelected(method) },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) AppColors.EmeraldAccent
                    else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(33.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = if (isSelected) AppColors.EmeraldAccent
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordPaymentBottomBar(
    enabled: Boolean,
    isSaving: Boolean,
    onClick: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 0.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.payment_record_button),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

/** Formats "2023-10-18" as "Oct 18, 2023". */
private fun formatDisplayDate(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val date = LocalDate.parse(iso.substring(0, 10))
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val month = months.getOrNull(date.month.ordinal) ?: "?"
        "$month ${date.day}, ${date.year}"
    } catch (e: Exception) {
        iso
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun AddPaymentScreenPreview() {
    RentManagerTheme {
        AddPaymentContent(
            state = AddPaymentState(
                amount = TextFieldValue("25000"),
                selectedPaymentMethod = PaymentMethod.CASH
            ),
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