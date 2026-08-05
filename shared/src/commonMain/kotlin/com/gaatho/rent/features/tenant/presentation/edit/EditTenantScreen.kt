package com.gaatho.rent.features.tenant.presentation.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.components.RentManagerButton
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppTextField
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
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
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = if (tenantId == "new") "Add Tenant" else "Edit Tenant",
                onBackClick = { viewModel.onAction(EditTenantAction.OnBackClicked) }
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
private fun EditTenantContent(
    state: EditTenantState,
    onAction: (EditTenantAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Photo placeholder
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val initials = state.name
                    .takeIf { it.isNotBlank() }
                    ?.split(" ")
                    ?.mapNotNull { it.firstOrNull()?.toString() }
                    ?.take(2)
                    ?.joinToString("") ?: "T"

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Edit tenant details below",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AppTextField(
                value = state.name,
                onValueChange = { onAction(EditTenantAction.OnNameChanged(it)) },
                label = "Full Name",
                placeholder = "e.g. Suman Shrestha",
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
            )
            if (state.nameError != null) {
                Text(
                    text = state.nameError,
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
                label = "Monthly Rent (NPR)",
                placeholder = "e.g. 15000",
                leadingIcon = {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
            )
            if (state.rentError != null) {
                Text(
                    text = state.rentError,
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
                label = "Assigned Property",
                placeholder = "Select Property",
                leadingIcon = {
                    Icon(Icons.Default.Home, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
            )

            Spacer(Modifier.height(32.dp))
        }

        // ── Footer ────────────────────────────────────────────────────────────
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
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.titleMedium
                        )
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
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Save",
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

// ─── Previews ─────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(name = "Edit Tenant — Filled")
@Composable
private fun EditTenantFilledPreview() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        EditTenantContent(
            state = EditTenantState(
                name = "Suman Shrestha",
                phone = "+977-9841234567",
                email = "suman@example.com",
                rentAmount = "15000",
                roomNumber = "2A",
                status = "Active",
                isLoading = false
            ),
            onAction = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Edit Tenant — Loading")
@Composable
private fun EditTenantLoadingPreview() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        EditTenantContent(
            state = EditTenantState(isLoading = true),
            onAction = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Edit Tenant — Saving")
@Composable
private fun EditTenantSavingPreview() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        EditTenantContent(
            state = EditTenantState(
                name = "Suman Shrestha",
                rentAmount = "15000",
                isLoading = false,
                isSaving = true
            ),
            onAction = {}
        )
    }
}
