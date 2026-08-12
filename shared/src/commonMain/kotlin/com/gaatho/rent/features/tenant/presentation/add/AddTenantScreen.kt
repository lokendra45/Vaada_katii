package com.gaatho.rent.features.tenant.presentation.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.components.RentManagerButton
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppTextField
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.koinInject
// import io.github.vinceglb.filekit.core.*
// import io.github.vinceglb.filekit.compose.*
// import io.github.vinceglb.filekit.dialogs.compose.*
// import io.github.vinceglb.filekit.*
import coil3.compose.AsyncImage
import androidx.compose.foundation.clickable
import com.gaatho.rent.core.ui.components.AppDocumentPicker
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantAction
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantSideEffect
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantState
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantViewModel
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*

// ─── Stateful entry point ─────────────────────────────────────────────────────
// Owns ViewModel, collects State & SideEffects, delegates rendering to stateless Content.

@Composable
fun AddTenantScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditTenantViewModel = koinInject(parameters = { parametersOf("new") })
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

    AddTenantContent(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

// ─── Stateless content ────────────────────────────────────────────────────────
// No ViewModel, no side effects. Pure state-in / events-out. Fully previewable.

@Composable
fun AddTenantContent(
    state: EditTenantState,
    onAction: (EditTenantAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    if (state.showSuccessDialog) {
        com.gaatho.rent.core.ui.components.AppDialog(
            variant = com.gaatho.rent.core.ui.components.AppDialog.Variant.Success,
            layout = com.gaatho.rent.core.ui.components.AppDialog.Layout.Center,
            icon = Icons.Default.CheckCircle,
            title = "Success!",
            body = "Tenant has been saved successfully.",
            confirmText = "OK",
            onConfirm = { onAction(EditTenantAction.OnSuccessDialogDismissed) },
            onDismiss = { onAction(EditTenantAction.OnSuccessDialogDismissed) }
        )
    }

    Scaffold(
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = "Add Tenant",
                onBackClick = { onAction(EditTenantAction.OnBackClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        val iconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        
        var avatarFile by remember { mutableStateOf<Any?>(null) }
        val avatarPicker = { }

        var citizenshipFront by remember { mutableStateOf<Any?>(null) }
        val ctzFrontPicker = { }
        
        var citizenshipBack by remember { mutableStateOf<Any?>(null) }
        val ctzBackPicker = {  }
        
        var passportDoc by remember { mutableStateOf<Any?>(null) }
        val passportPicker = {  }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            // ── Scrollable form ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                // Avatar header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val initials = state.name.text
                        .takeIf { it.isNotBlank() }
                        ?.split(" ")
                        ?.mapNotNull { it.firstOrNull()?.toString() }
                        ?.take(2)
                        ?.joinToString("") ?: "+"

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        // Camera overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(bottom = 8.dp).size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "New Tenant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Fill in the details to add a tenant",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AppTextField(
                    value = state.name,
                    onValueChange = { onAction(EditTenantAction.OnNameChanged(it)) },
                    label = "Full Name *",
                    placeholder = "e.g. Suman Shrestha",
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                )
                if (state.nameError != null) {
                    Text(
                        text = state.nameError ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                AppTextField(
                    value = state.phone,
                    onValueChange = { onAction(EditTenantAction.OnPhoneChanged(it)) },
                    label = "Phone Number",
                    placeholder = "e.g. +977-9841234567",
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                )

                Spacer(Modifier.height(16.dp))

                AppTextField(
                    value = state.email,
                    onValueChange = { onAction(EditTenantAction.OnEmailChanged(it)) },
                    label = "Email Address",
                    placeholder = "e.g. suman@example.com",
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                )

                Spacer(Modifier.height(16.dp))

                AppTextField(
                    value = state.rentAmount,
                    onValueChange = { onAction(EditTenantAction.OnRentChanged(it)) },
                    label = "Monthly Rent (NPR) *",
                    placeholder = "e.g. 15000",
                    leadingIcon = {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                )
                if (state.rentError != null) {
                    Text(
                        text = state.rentError ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AppTextField(
                        value = state.roomNumber,
                        onValueChange = { onAction(EditTenantAction.OnRoomNumberChanged(it)) },
                        label = "Room / Flat",
                        placeholder = "e.g. 2A",
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(Icons.Default.DoorFront, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                        }
                    )

                    val statusOptions = persistentListOf("Active", "Inactive", "Overdue")
                    AppDropdown(
                        options = statusOptions,
                        selectedItem = state.status,
                        onItemSelected = { onAction(EditTenantAction.OnStatusSelected(it)) },
                        label = "Status",
                        placeholder = "Active",
                        modifier = Modifier.weight(1.2f),
                        leadingIcon = {
                            Icon(Icons.Default.Info, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                val propertyNames = state.propertyOptions.map { it.name }.toPersistentList()
                val selectedPropertyName = state.propertyOptions.find { it.id == state.propertyId }?.name ?: ""
                AppDropdown(
                    options = propertyNames,
                    selectedItem = selectedPropertyName,
                    onItemSelected = { name ->
                        val id = state.propertyOptions.find { it.name == name }?.id
                        if (id != null) onAction(EditTenantAction.OnPropertySelected(id))
                    },
                    label = "Assign to Property",
                    placeholder = "Select Property",
                    leadingIcon = {
                        Icon(Icons.Default.Home, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                )

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(24.dp))

                Text(
                    text = stringResource(Res.string.tenant_documents_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(Res.string.tenant_documents_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AppDocumentPicker(
                        title = stringResource(Res.string.citizenship_front),
                        file = citizenshipFront,
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    )
                    AppDocumentPicker(
                        title = stringResource(Res.string.citizenship_back),
                        file = citizenshipBack,
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                AppDocumentPicker(
                    title = stringResource(Res.string.passport_optional),
                    file = passportDoc,
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(32.dp))
            }

            // ── Footer ────────────────────────────────────────────────────────
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onAction(EditTenantAction.OnBackClicked) },
                            modifier = Modifier
                                .weight(1f)
                                .height(AppDimensions.ButtonHeightMedium),
                            shape = RoundedCornerShape(AppDimensions.RadiusPill),
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(text = "Cancel", style = MaterialTheme.typography.titleMedium)
                        }

                        RentManagerButton(
                            onClick = { onAction(EditTenantAction.OnSaveClicked) },
                            modifier = Modifier.weight(1.5f)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Add Tenant",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Add Tenant — Empty")
@Composable
private fun AddTenantEmptyPreview() {
    RentManagerTheme {
        AddTenantContent(state = EditTenantState(), onAction = {})
    }
}

@Preview(name = "Add Tenant — Filled")
@Composable
private fun AddTenantFilledPreview() {
    RentManagerTheme {
        AddTenantContent(
            state = EditTenantState(
                name = androidx.compose.ui.text.input.TextFieldValue("Suman Shrestha"),
                phone = androidx.compose.ui.text.input.TextFieldValue("+977-9841234567"),
                rentAmount = androidx.compose.ui.text.input.TextFieldValue("15000"),
                roomNumber = androidx.compose.ui.text.input.TextFieldValue("2A"),
                status = "Active"
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Add Tenant — Saving")
@Composable
private fun AddTenantSavingPreview() {
    RentManagerTheme {
        AddTenantContent(
            state = EditTenantState(
                name = androidx.compose.ui.text.input.TextFieldValue("Suman Shrestha"),
                isSaving = true
            ),
            onAction = {}
        )
    }
}
