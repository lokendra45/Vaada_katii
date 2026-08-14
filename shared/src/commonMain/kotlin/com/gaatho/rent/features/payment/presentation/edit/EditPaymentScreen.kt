package com.gaatho.rent.features.payment.presentation.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.components.RentManagerOutlinedButton
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppCard
import com.gaatho.rent.core.ui.components.AppDatePickerDialog
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.payment.presentation.add.PaymentMethod
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun EditPaymentScreen(
    paymentId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: EditPaymentViewModel = koinViewModel(parameters = { parametersOf(paymentId) })
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is EditPaymentSideEffect.NavigateBack -> onNavigateBack()
            is EditPaymentSideEffect.ShowSnackbar ->
                snackbarHostState.showSnackbar(effect.message)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Edit Payment Details",
                onBackClick = { viewModel.onAction(EditPaymentAction.OnBackClicked) },
                containerColor = MaterialTheme.colorScheme.background,
                titleStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        EditPaymentContent(
            state = state,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun EditPaymentContent(
    state: EditPaymentState,
    onAction: (EditPaymentAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.showSuccessDialog) {
        AppDialog(
            variant = AppDialog.Variant.Success,
            layout = AppDialog.Layout.Center,
            icon = Icons.Default.CheckCircle,
            title = "Success!",
            body = "Payment details have been updated.",
            confirmText = "OK",
            onConfirm = { onAction(EditPaymentAction.OnSuccessDialogDismissed) },
            onDismiss = { onAction(EditPaymentAction.OnSuccessDialogDismissed) }
        )
    }

    if (state.showDatePicker) {
        AppDatePickerDialog(
            selectedDate = state.paymentDate,
            onDateSelected = { onAction(EditPaymentAction.OnPaymentDateChanged(it)) },
            onDismiss = { onAction(EditPaymentAction.OnDatePickerDismissed) },
            title = "Payment Date"
        )
    }

    val scrollState = rememberScrollState()

    val labelStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
    val fieldStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    )
    val fieldShape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val load = state.loadState) {
            is UiState.Loading, UiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(text = load.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    ReadOnlySection(data = load.data)

                    Spacer(Modifier.height(16.dp))

                    AppTextField(
                        value = state.amount,
                        onValueChange = { onAction(EditPaymentAction.OnAmountChanged(it)) },
                        label = "Payment Amount (NPR)",
                        placeholder = "e.g. 25,000",
                        labelStyle = labelStyle,
                        fieldTextStyle = fieldStyle,
                        shape = fieldShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    AppTextField(
                        value = DateTimeUtil.formatDisplayDate(state.paymentDate),
                        onValueChange = {},
                        readOnly = true,
                        label = "Payment Date",
                        placeholder = "Select date",
                        labelStyle = labelStyle,
                        fieldTextStyle = fieldStyle,
                        shape = fieldShape,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = { onAction(EditPaymentAction.OnDateFieldClicked) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    val methods = persistentListOf(
                        PaymentMethod.CASH, PaymentMethod.ESEWA, PaymentMethod.KHALTI, PaymentMethod.BANK_TRANSFER
                    )
                    AppDropdown(
                        options = methods,
                        selectedItem = state.selectedMethod,
                        onItemSelected = { onAction(EditPaymentAction.OnMethodSelected(it)) },
                        itemLabel = { it.displayName },
                        label = "Payment Method",
                        placeholder = "Select method",
                        labelStyle = labelStyle,
                        fieldTextStyle = fieldStyle,
                        shape = fieldShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    AppTextField(
                        value = state.receiptNumber,
                        onValueChange = { onAction(EditPaymentAction.OnReceiptNumberChanged(it)) },
                        label = "Receipt Number",
                        placeholder = "e.g. TXN-98231089201",
                        labelStyle = labelStyle,
                        fieldTextStyle = fieldStyle,
                        shape = fieldShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    AppTextField(
                        value = state.notes,
                        onValueChange = { onAction(EditPaymentAction.OnNotesChanged(it)) },
                        label = "Notes",
                        placeholder = "e.g. October rent paid completely.",
                        singleLine = false,
                        minLines = 3,
                        labelStyle = labelStyle,
                        fieldTextStyle = fieldStyle,
                        shape = fieldShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))
                }

                // ── Footer actions ──────────────────────────────────────────
                Surface(color = MaterialTheme.colorScheme.background) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RentManagerOutlinedButton(
                            text = "Delete Payment",
                            onClick = { onAction(EditPaymentAction.OnDeleteClicked) },
                            modifier = Modifier.weight(1f),
                            borderColor = AppColors.Error,
                            contentColor = AppColors.Error
                        )
                        RentManagerPrimaryButton(
                            text = "Update Payment",
                            onClick = { onAction(EditPaymentAction.OnSaveClicked) },
                            modifier = Modifier.weight(1f),
                            enabled = state.canSubmit,
                            isLoading = state.isSaving
                        )
                    }
                }
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { onAction(EditPaymentAction.OnDeleteDismissed) },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Payment?") },
            text = { Text("This will permanently delete this payment. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { onAction(EditPaymentAction.OnDeleteConfirmed) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(EditPaymentAction.OnDeleteDismissed) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ─── Read-only tenant / property section ──────────────────────────────────────

@Composable
private fun ReadOnlySection(data: EditPaymentData) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        useCardShadow = false,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tenant",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = data.tenantName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Property / Unit",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = data.propertyUnit,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Edit Payment — Filled")
@Composable
private fun EditPaymentFilledPreview() {
    RentManagerTheme {
        EditPaymentContent(
            state = EditPaymentState(
                loadState = UiState.Success(
                    EditPaymentData(
                        paymentId = "p1",
                        tenantName = "Suman Maharjan",
                        propertyUnit = "Baluwatar House - Unit 2B"
                    )
                ),
                amount = TextFieldValue("25000"),
                paymentDate = "2023-10-18",
                selectedMethod = PaymentMethod.ESEWA,
                receiptNumber = TextFieldValue("TXN-98231089201"),
                notes = TextFieldValue("October rent paid completely. Happy with prompt response.")
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Edit Payment — Loading")
@Composable
private fun EditPaymentLoadingPreview() {
    RentManagerTheme {
        EditPaymentContent(
            state = EditPaymentState(),
            onAction = {}
        )
    }
}
