package com.gaatho.rent.features.tenant.presentation.add

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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.ui.components.AppBottomActionBar
import com.gaatho.rent.core.ui.components.AppDatePickerDialog
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.core.ui.components.AppDocumentPicker
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantAction
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantSideEffect
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantState
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantViewModel
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.add_tenant_btn
import rentmanagerapp.shared.generated.resources.add_tenant_screen_title
import rentmanagerapp.shared.generated.resources.assign_property_label
import rentmanagerapp.shared.generated.resources.assign_property_placeholder
import rentmanagerapp.shared.generated.resources.continue_btn
import rentmanagerapp.shared.generated.resources.id_proof_upload_label
import rentmanagerapp.shared.generated.resources.lease_duration_label
import rentmanagerapp.shared.generated.resources.lease_duration_placeholder
import rentmanagerapp.shared.generated.resources.move_in_date_label
import rentmanagerapp.shared.generated.resources.move_in_date_placeholder
import rentmanagerapp.shared.generated.resources.rent_amount_label
import rentmanagerapp.shared.generated.resources.rent_amount_placeholder
import rentmanagerapp.shared.generated.resources.security_deposit_label
import rentmanagerapp.shared.generated.resources.security_deposit_placeholder
import rentmanagerapp.shared.generated.resources.tenant_email_label
import rentmanagerapp.shared.generated.resources.tenant_email_placeholder
import rentmanagerapp.shared.generated.resources.tenant_full_name_label
import rentmanagerapp.shared.generated.resources.tenant_full_name_placeholder
import rentmanagerapp.shared.generated.resources.tenant_phone_label
import rentmanagerapp.shared.generated.resources.tenant_phone_placeholder
import rentmanagerapp.shared.generated.resources.tenant_saved_body
import rentmanagerapp.shared.generated.resources.tenant_success_title
import rentmanagerapp.shared.generated.resources.unit_number_label
import rentmanagerapp.shared.generated.resources.unit_number_placeholder

// ─── Stateful entry point ─────────────────────────────────────────────────────

@Composable
fun AddTenantScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditTenantViewModel = koinInject(parameters = { parametersOf("new") })
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is EditTenantSideEffect.NavigateBack -> onNavigateBack()
            is EditTenantSideEffect.ShowSnackbar ->
                snackbarHostState.showSnackbar(effect.message)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.add_tenant_screen_title),
                onBackClick = { viewModel.onAction(EditTenantAction.OnBackClicked) },
                containerColor = MaterialTheme.colorScheme.background,
                titleStyle = MaterialTheme.typography.headlineMedium
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        AddTenantContent(
            state = state,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

// ─── Stateless content ────────────────────────────────────────────────────────

@Composable
fun AddTenantContent(
    state: EditTenantState,
    onAction: (EditTenantAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.showSuccessDialog) {
        AppDialog(
            variant = AppDialog.Variant.Success,
            layout = AppDialog.Layout.Center,
            icon = Icons.Default.CheckCircle,
            title = stringResource(Res.string.tenant_success_title),
            body = stringResource(Res.string.tenant_saved_body),
            confirmText = stringResource(Res.string.continue_btn),
            onConfirm = { onAction(EditTenantAction.OnSuccessDialogDismissed) },
            onDismiss = { onAction(EditTenantAction.OnSuccessDialogDismissed) }
        )
    }

    val scrollState = rememberScrollState()

    val fieldShape = RoundedCornerShape(12.dp)

    val propertyNames = remember(state.propertyOptions) {
        state.propertyOptions.map { it.name }.let(::persistentListOf)
    }
    val selectedPropertyName = state.propertyOptions.find { it.id == state.propertyId }?.name

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        AppDatePickerDialog(
            selectedDate = state.moveInDate,
            onDateSelected = {
                onAction(EditTenantAction.OnMoveInDateChanged(it))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            title = "Move-In Date"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.name,
                onValueChange = { onAction(EditTenantAction.OnNameChanged(it)) },
                label = stringResource(Res.string.tenant_full_name_label),
                placeholder = stringResource(Res.string.tenant_full_name_placeholder),
                errorMessage = state.nameError,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.phone,
                onValueChange = { onAction(EditTenantAction.OnPhoneChanged(it)) },
                label = stringResource(Res.string.tenant_phone_label),
                placeholder = stringResource(Res.string.tenant_phone_placeholder),
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.email,
                onValueChange = { onAction(EditTenantAction.OnEmailChanged(it)) },
                label = stringResource(Res.string.tenant_email_label),
                placeholder = stringResource(Res.string.tenant_email_placeholder),
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            AppDropdown(
                options = propertyNames,
                selectedItem = selectedPropertyName,
                onItemSelected = { name ->
                    val id = state.propertyOptions.find { it.name == name }?.id
                    if (id != null) onAction(EditTenantAction.OnPropertySelected(id))
                },
                label = stringResource(Res.string.assign_property_label),
                placeholder = stringResource(Res.string.assign_property_placeholder),
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Unit Number + Rent Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = state.unitNumber,
                    onValueChange = { onAction(EditTenantAction.OnUnitNumberChanged(it)) },
                    label = stringResource(Res.string.unit_number_label),
                    placeholder = stringResource(Res.string.unit_number_placeholder),
                    shape = fieldShape,
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = state.rentAmount,
                    onValueChange = { onAction(EditTenantAction.OnRentChanged(it)) },
                    label = stringResource(Res.string.rent_amount_label),
                    placeholder = stringResource(Res.string.rent_amount_placeholder),
                    errorMessage = state.rentError,
                    shape = fieldShape,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Move-In Date + Lease Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = DateTimeUtil.formatDisplayDate(state.moveInDate),
                    onValueChange = {},
                    readOnly = true,
                    label = stringResource(Res.string.move_in_date_label),
                    placeholder = stringResource(Res.string.move_in_date_placeholder),
                    shape = fieldShape,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                )

                val leaseDurations = persistentListOf("1 Year", "2 Years", "3 Years", "5 Years")
                AppDropdown(
                    options = leaseDurations,
                    selectedItem = state.leaseDuration,
                    onItemSelected = { onAction(EditTenantAction.OnLeaseDurationSelected(it)) },
                    label = stringResource(Res.string.lease_duration_label),
                    placeholder = stringResource(Res.string.lease_duration_placeholder),
                    shape = fieldShape,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.securityDeposit,
                onValueChange = { onAction(EditTenantAction.OnSecurityDepositChanged(it)) },
                label = stringResource(Res.string.security_deposit_label),
                placeholder = stringResource(Res.string.security_deposit_placeholder),
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            AppDocumentPicker(
                title = stringResource(Res.string.id_proof_upload_label),
                file = state.uploadedDocumentName,
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }

        // ── Submit ──────────────────────────────────────────────────────────
        AppBottomActionBar {
            RentManagerPrimaryButton(
                text = stringResource(Res.string.add_tenant_btn),
                onClick = { onAction(EditTenantAction.OnSaveClicked) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = state.isSaving
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Add Tenant — Empty")
@Composable
private fun AddTenantEmptyPreview() {
    RentManagerTheme {
        AddTenantContent(state = EditTenantState(isLoading = false), onAction = {})
    }
}

@Preview(name = "Add Tenant — Filled")
@Composable
private fun AddTenantFilledPreview() {
    RentManagerTheme {
        AddTenantContent(
            state = EditTenantState(
                name = TextFieldValue("Suman Maharjan"),
                phone = TextFieldValue("9841876543"),
                email = TextFieldValue("suman.maharjan@gmail.com"),
                rentAmount = TextFieldValue("25000"),
                unitNumber = TextFieldValue("Unit 2B"),
                moveInDate = "2023-10-18",
                leaseDuration = "1 Year",
                securityDeposit = TextFieldValue("50000"),
                isLoading = false,
                propertyId = "prop-1",
                propertyOptions = listOf(com.gaatho.rent.features.tenant.presentation.edit.PropertyOption("prop-1", "Baluwatar House"))
            ),
            onAction = {}
        )
    }
}
