package com.gaatho.rent.features.property.presentation.add

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
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.gaatho.rent.features.property.presentation.edit.EditPropertyAction
import com.gaatho.rent.features.property.presentation.edit.EditPropertySideEffect
import com.gaatho.rent.features.property.presentation.edit.EditPropertyState
import com.gaatho.rent.features.property.presentation.edit.EditPropertyViewModel

// ─── Stateful entry point ─────────────────────────────────────────────────────
// Owns ViewModel, collects State & SideEffects, delegates rendering to stateless Content.

@Composable
fun AddPropertyScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditPropertyViewModel = koinInject(parameters = { parametersOf("new") })
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is EditPropertySideEffect.NavigateBack -> onNavigateBack()
            is EditPropertySideEffect.ShowSnackbar ->
                snackbarHostState.showSnackbar(effect.message)
        }
    }

    AddPropertyContent(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

// ─── Stateless content ────────────────────────────────────────────────────────
// No ViewModel, no side effects. Pure state-in / events-out. Fully previewable.

@Composable
fun AddPropertyContent(
    state: EditPropertyState,
    onAction: (EditPropertyAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    if (state.showSuccessDialog) {
        com.gaatho.rent.core.ui.components.AppDialog(
            variant = com.gaatho.rent.core.ui.components.AppDialog.Variant.Success,
            layout = com.gaatho.rent.core.ui.components.AppDialog.Layout.Center,
            icon = Icons.Default.CheckCircle,
            title = "Success!",
            body = "Property has been saved successfully.",
            confirmText = "OK",
            onConfirm = { onAction(EditPropertyAction.OnSuccessDialogDismissed) },
            onDismiss = { onAction(EditPropertyAction.OnSuccessDialogDismissed) }
        )
    }

    Scaffold(
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = "Add Property",
                onBackClick = { onAction(EditPropertyAction.OnBackClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        val iconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

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
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddHome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "New Property",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Fill in the details to register a property",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Property Name
                AppTextField(
                    value = state.name,
                    onValueChange = { onAction(EditPropertyAction.OnNameChanged(it)) },
                    label = "Property Name *",
                    placeholder = "e.g. Peaceful Villa",
                    leadingIcon = {
                        Icon(Icons.Default.Business, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
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

                // Street address
                AppTextField(
                    value = state.streetAddress,
                    onValueChange = { onAction(EditPropertyAction.OnStreetAddressChanged(it)) },
                    label = "Street Address *",
                    placeholder = "e.g. 123 Main St",
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                )
                if (state.addressError != null) {
                    Text(
                        text = state.addressError ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                AppTextField(
                    value = state.city,
                    onValueChange = { onAction(EditPropertyAction.OnCityChanged(it)) },
                    label = "City / Area",
                    placeholder = "e.g. Kathmandu",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Type + Units row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val propertyTypes = persistentListOf("HOUSE", "APARTMENT", "FLAT", "SHOP", "BUILDING")
                    AppDropdown(
                        options = propertyTypes,
                        selectedItem = state.propertyType,
                        onItemSelected = { onAction(EditPropertyAction.OnTypeChanged(it)) },
                        label = "Type",
                        placeholder = "Select...",
                        modifier = Modifier.weight(1.2f),
                        leadingIcon = {
                            Icon(Icons.Default.Category, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                        }
                    )
                    AppTextField(
                        value = state.totalUnits,
                        onValueChange = { onAction(EditPropertyAction.OnTotalUnitsChanged(it)) },
                        label = "Total Units",
                        placeholder = "1",
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(Icons.Default.GridView, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Billing cycle
                val billingCycles = persistentListOf(
                    "1st of the month", "15th of the month", "Last day of the month"
                )
                AppDropdown(
                    options = billingCycles,
                    selectedItem = state.billingCycle,
                    onItemSelected = { onAction(EditPropertyAction.OnBillingCycleChanged(it)) },
                    label = "Billing Cycle",
                    placeholder = "1st of the month",
                    leadingIcon = {
                        Icon(Icons.Default.Event, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                )

                Spacer(Modifier.height(24.dp))

                // Amenities
                Text(
                    text = "Amenities & Utilities Managed",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val amenities = listOf("Water", "Electricity", "Internet", "Trash")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in amenities.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AmenityChip(
                                label = amenities[i],
                                isSelected = state.selectedAmenities.contains(amenities[i]),
                                onToggle = { onAction(EditPropertyAction.OnAmenityToggled(amenities[i])) },
                                modifier = Modifier.weight(1f)
                            )
                            if (i + 1 < amenities.size) {
                                AmenityChip(
                                    label = amenities[i + 1],
                                    isSelected = state.selectedAmenities.contains(amenities[i + 1]),
                                    onToggle = { onAction(EditPropertyAction.OnAmenityToggled(amenities[i + 1])) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

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
                            onClick = { onAction(EditPropertyAction.OnBackClicked) },
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
                            onClick = { onAction(EditPropertyAction.OnSaveClicked) },
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
                                    Icon(Icons.Default.AddHome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Add Property",
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

// ─── Amenity chip ─────────────────────────────────────────────────────────────

@Composable
private fun AmenityChip(
    label: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 0.dp,
        modifier = modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Add Property — Empty")
@Composable
private fun AddPropertyEmptyPreview() {
    RentManagerTheme {
        AddPropertyContent(state = EditPropertyState(), onAction = {})
    }
}

@Preview(name = "Add Property — Filled")
@Composable
private fun AddPropertyFilledPreview() {
    RentManagerTheme {
        AddPropertyContent(
            state = EditPropertyState(
                name = androidx.compose.ui.text.input.TextFieldValue("Peaceful Villa"),
                streetAddress = androidx.compose.ui.text.input.TextFieldValue("Koteshwor"),
                city = androidx.compose.ui.text.input.TextFieldValue("Kathmandu"),
                propertyType = "HOUSE",
                totalUnits = androidx.compose.ui.text.input.TextFieldValue("4"),
                selectedAmenities = setOf("Water", "Electricity")
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Add Property — Saving")
@Composable
private fun AddPropertySavingPreview() {
    RentManagerTheme {
        AddPropertyContent(
            state = EditPropertyState(
                name = androidx.compose.ui.text.input.TextFieldValue("Peaceful Villa"),
                isSaving = true
            ),
            onAction = {}
        )
    }
}
