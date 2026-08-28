package com.gaatho.rent.features.payment.presentation.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.cancel_action
import rentmanagerapp.shared.generated.resources.delete_action
import rentmanagerapp.shared.generated.resources.delete_payment_desc
import rentmanagerapp.shared.generated.resources.delete_payment_title

@Composable
fun EditPaymentScreen(
    paymentId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: EditPaymentViewModel = koinViewModel(parameters = { parametersOf(paymentId) })
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
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
                titleStyle = MaterialTheme.typography.titleMedium
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
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp)
                        .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    Spacer(Modifier.height(16.dp))

                    ReadOnlySection(data = load.data)

                    Spacer(Modifier.height(16.dp))

                    AppTextField(
                        value = state.amount,
                        onValueChange = { onAction(EditPaymentAction.OnAmountChanged(it)) },
                        label = "Payment Amount (NPR)",
                        placeholder = "e.g. 25,000",
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

                    AppDropdown(
                        options = state.availableMethods,
                        selectedItem = state.selectedMethod,
                        onItemSelected = { onAction(EditPaymentAction.OnMethodSelected(it)) },
                        itemLabel = { it.displayName },
                        label = "Payment Method",
                        placeholder = "Select method",
                        shape = fieldShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    AppTextField(
                        value = state.receiptNumber,
                        onValueChange = { onAction(EditPaymentAction.OnReceiptNumberChanged(it)) },
                        label = "Receipt Number",
                        placeholder = "e.g. TXN-98231089201",
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
        com.gaatho.rent.core.ui.components.AppDialog(
            icon = Icons.Default.Delete,
            title = stringResource(Res.string.delete_payment_title),
            body = stringResource(Res.string.delete_payment_desc),
            confirmText = stringResource(Res.string.delete_action),
            dismissText = stringResource(Res.string.cancel_action),
            onConfirm = { onAction(EditPaymentAction.OnDeleteConfirmed) },
            onDismiss = { onAction(EditPaymentAction.OnDeleteDismissed) },
            variant = com.gaatho.rent.core.ui.components.AppDialog.Variant.Destructive
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
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = data.tenantName,
                    style = MaterialTheme.typography.bodyMedium,
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
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = data.propertyUnit,
                    style = MaterialTheme.typography.bodyMedium,
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
