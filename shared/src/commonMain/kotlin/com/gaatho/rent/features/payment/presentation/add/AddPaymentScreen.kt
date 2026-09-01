package com.gaatho.rent.features.payment.presentation.add


import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppDateField
import com.gaatho.rent.core.ui.components.*
import com.gaatho.rent.core.ui.components.AppDatePickerDialog
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppSnackbarHost
import com.gaatho.rent.core.ui.components.AppSnackbarVariant
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.core.ui.components.rememberAppSnackbarState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import rentmanagerapp.shared.generated.resources.assign_property_label
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
import rentmanagerapp.shared.generated.resources.payment_property_unit_placeholder
import rentmanagerapp.shared.generated.resources.payment_record_button
import rentmanagerapp.shared.generated.resources.payment_remarks_label
import rentmanagerapp.shared.generated.resources.payment_remarks_placeholder
import rentmanagerapp.shared.generated.resources.payment_select_property_first
import rentmanagerapp.shared.generated.resources.payment_select_unit
import rentmanagerapp.shared.generated.resources.payment_tenant_label
import rentmanagerapp.shared.generated.resources.payment_tenant_placeholder
import rentmanagerapp.shared.generated.resources.unit_number_label

@Composable
fun AddPaymentScreen(
    tenantIdArg: String? = null,
    propertyIdArg: String? = null,
    onNavigateBack: () -> Unit
) {
    val viewModel: AddPaymentViewModel = koinViewModel(parameters = { org.koin.core.parameter.parametersOf(tenantIdArg, propertyIdArg) })
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
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
    val fieldShape = RoundedCornerShape(10.dp)

    if (state.showDatePicker) {
        AppDatePickerDialog(
            selectedDate = state.paymentDate,
            onDateSelected = { onAction(AddPaymentAction.OnPaymentDateChanged(it)) },
            onDismiss = { onAction(AddPaymentAction.OnDatePickerDismissed) },
            title = stringResource(Res.string.payment_date_label)
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
        // Guard: need at least one active tenant to record a payment
        val dataLoaded = state.tenantsState is UiState.Success
        if (dataLoaded && state.allTenants.isEmpty()) {
            NoTenantsEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            Spacer(Modifier.height(16.dp))

            // Property (required)
            AppDropdown(
                options = state.propertyItems,
                selectedItem = state.selectedProperty,
                itemLabel = { it.name },
                onItemSelected = { onAction(AddPaymentAction.OnPropertySelected(it.id)) },
                label = stringResource(Res.string.assign_property_label),
                placeholder = stringResource(Res.string.payment_property_unit_placeholder),
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Select Tenant (enabled only after property selection)
            AppDropdown(
                options = state.tenantItems,
                selectedItem = state.selectedTenant,
                itemLabel = { it.name },
                onItemSelected = { onAction(AddPaymentAction.OnTenantSelected(it.id)) },
                label = stringResource(Res.string.payment_tenant_label),
                placeholder = if (state.selectedProperty == null) 
                    stringResource(Res.string.payment_select_property_first) else stringResource(Res.string.payment_tenant_placeholder),
                shape = fieldShape,
                enabled = state.selectedProperty != null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Unit (required once a property is selected)
            AppDropdown(
                options = state.unitOptions,
                selectedItem = state.selectedUnit,
                itemLabel = { it },
                onItemSelected = { onAction(AddPaymentAction.OnUnitSelected(it)) },
                label = stringResource(Res.string.unit_number_label),
                placeholder = if (state.selectedProperty == null)
                    stringResource(Res.string.payment_select_property_first) else stringResource(Res.string.payment_select_unit),
                shape = fieldShape,
                enabled = state.selectedProperty != null,
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
                shape = fieldShape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Payment Method
            Column {
                LabelText(
                    text = stringResource(Res.string.payment_method),
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
            AppDateField(
                value = state.paymentDate,
                onClick = { onAction(AddPaymentAction.OnDateFieldClicked) },
                label = stringResource(Res.string.payment_date_label),
                placeholder = stringResource(Res.string.payment_date_placeholder),
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Note (Optional)
            AppTextField(
                value = state.remarks,
                onValueChange = { onAction(AddPaymentAction.OnRemarksChanged(it)) },
                label = stringResource(Res.string.payment_remarks_label),
                placeholder = stringResource(Res.string.payment_remarks_placeholder),
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
        LabelText(
            text = label,
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
                BodyText(
                    text = if (text.isEmpty()) placeholder else text,
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
                    LabelText(
                        text = label,
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
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.payment_record_button),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun NoTenantsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.PersonAdd,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(24.dp))
        SectionTitle(
            text = stringResource(Res.string.payment_no_active_tenants),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        BodyText(
            text = stringResource(Res.string.payment_no_active_tenants_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
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

