package com.gaatho.rent.features.tenant.presentation.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
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
import com.gaatho.rent.core.ui.components.AppCard
import com.gaatho.rent.core.ui.components.AppDatePickerDialog
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.core.utils.DateTimeUtil
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun EditTenantScreen(
    tenantId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditTenantViewModel = koinInject(parameters = { parametersOf(tenantId) })
) {
    val state by viewModel.collectAsState()
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
                title = "Edit Tenant Details",
                onBackClick = { viewModel.onAction(EditTenantAction.OnBackClicked) },
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
        EditTenantContent(
            state = state,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun EditTenantContent(
    state: EditTenantState,
    onAction: (EditTenantAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.showSuccessDialog) {
        AppDialog(
            variant = AppDialog.Variant.Success,
            layout = AppDialog.Layout.Center,
            icon = Icons.Default.CheckCircle,
            title = "Success!",
            body = "Tenant has been saved successfully.",
            confirmText = "OK",
            onConfirm = { onAction(EditTenantAction.OnSuccessDialogDismissed) },
            onDismiss = { onAction(EditTenantAction.OnSuccessDialogDismissed) }
        )
    }

    val scrollState = rememberScrollState()

    val labelStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
    val fieldStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    )
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
                label = "Tenant Full Name",
                placeholder = "e.g. Suman Maharjan",
                errorMessage = state.nameError,
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.phone,
                onValueChange = { onAction(EditTenantAction.OnPhoneChanged(it)) },
                label = "Phone Number",
                placeholder = "e.g. 9841XXXXXX",
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.email,
                onValueChange = { onAction(EditTenantAction.OnEmailChanged(it)) },
                label = "Email Address",
                placeholder = "e.g. name@domain.com",
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
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
                label = "Assign Property",
                placeholder = "Select Property",
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = state.unitNumber,
                    onValueChange = { onAction(EditTenantAction.OnUnitNumberChanged(it)) },
                    label = "Unit Number",
                    placeholder = "e.g. Unit 2B",
                    labelStyle = labelStyle,
                    fieldTextStyle = fieldStyle,
                    shape = fieldShape,
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = state.rentAmount,
                    onValueChange = { onAction(EditTenantAction.OnRentChanged(it)) },
                    label = "Rent Amount (NPR)",
                    placeholder = "e.g. 25,000",
                    errorMessage = state.rentError,
                    labelStyle = labelStyle,
                    fieldTextStyle = fieldStyle,
                    shape = fieldShape,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = DateTimeUtil.formatDisplayDate(state.moveInDate),
                    onValueChange = {},
                    readOnly = true,
                    label = "Move-In Date",
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
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                )

                val leaseDurations = persistentListOf("1 Year", "2 Years", "3 Years", "5 Years")
                AppDropdown(
                    options = leaseDurations,
                    selectedItem = state.leaseDuration,
                    onItemSelected = { onAction(EditTenantAction.OnLeaseDurationSelected(it)) },
                    label = "Lease Duration",
                    placeholder = "1 Year",
                    labelStyle = labelStyle,
                    fieldTextStyle = fieldStyle,
                    shape = fieldShape,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.securityDeposit,
                onValueChange = { onAction(EditTenantAction.OnSecurityDepositChanged(it)) },
                label = "Security Deposit (NPR)",
                placeholder = "e.g. 50,000",
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            IdentityDocumentCard(fileName = state.uploadedDocumentName)

            Spacer(Modifier.height(24.dp))
        }

        // ── Footer actions ──────────────────────────────────────────────────
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RentManagerOutlinedButton(
                    text = "Remove Tenant",
                    onClick = { onAction(EditTenantAction.OnDeleteClicked) },
                    modifier = Modifier.weight(1f),
                    borderColor = AppColors.Error,
                    contentColor = AppColors.Error
                )
                RentManagerPrimaryButton(
                    text = "Save Changes",
                    onClick = { onAction(EditTenantAction.OnSaveClicked) },
                    modifier = Modifier.weight(1f),
                    isLoading = state.isSaving
                )
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { onAction(EditTenantAction.OnDeleteDismissed) },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Remove Tenant?") },
            text = { Text("This will permanently remove this tenant. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { onAction(EditTenantAction.OnDeleteConfirmed) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(EditTenantAction.OnDeleteDismissed) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ─── Identity document card ───────────────────────────────────────────────────

@Composable
private fun IdentityDocumentCard(fileName: String?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Identity Proof Document",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            useCardShadow = false
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = fileName ?: "suman_nagarikta.pdf",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    text = "Replace",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = AppColors.Error
                    )
                )
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Edit Tenant — Filled")
@Composable
private fun EditTenantFilledPreview() {
    RentManagerTheme {
        EditTenantContent(
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
                propertyOptions = listOf(PropertyOption("prop-1", "Baluwatar House"))
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Edit Tenant — Loading")
@Composable
private fun EditTenantLoadingPreview() {
    RentManagerTheme {
        EditTenantContent(
            state = EditTenantState(isLoading = true),
            onAction = {}
        )
    }
}
