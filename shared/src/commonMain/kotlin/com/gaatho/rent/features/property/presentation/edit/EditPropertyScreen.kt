package com.gaatho.rent.features.property.presentation.edit

import androidx.compose.foundation.*
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

// ─── Entry point ──────────────────────────────────────────────────────────────

@Composable
fun EditPropertyScreen(
    propertyId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditPropertyViewModel = koinInject(parameters = { parametersOf(propertyId) })
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

    Scaffold(
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = "Edit Property",
                onBackClick = { viewModel.onAction(EditPropertyAction.OnBackClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        EditPropertyContent(
            state = state,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPropertyContent(
    state: EditPropertyState,
    onAction: (EditPropertyAction) -> Unit,
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

        // ── Scrollable form ───────────────────────────────────────────────────
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
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Edit property details below",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Property Name
            AppTextField(
                value = state.name,
                onValueChange = { onAction(EditPropertyAction.OnNameChanged(it)) },
                label = "Property Name",
                placeholder = "e.g. Peaceful Villa",
                leadingIcon = {
                    Icon(
                        Icons.Default.Business, contentDescription = null,
                        tint = iconTint, modifier = Modifier.size(20.dp)
                    )
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

            // Street address
            AppTextField(
                value = state.streetAddress,
                onValueChange = { onAction(EditPropertyAction.OnStreetAddressChanged(it)) },
                label = "Street Address",
                placeholder = "e.g. 123 Main St",
                leadingIcon = {
                    Icon(
                        Icons.Default.LocationOn, contentDescription = null,
                        tint = iconTint, modifier = Modifier.size(20.dp)
                    )
                }
            )
            if (state.addressError != null) {
                Text(
                    text = state.addressError,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // City + Zip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTextField(
                    value = state.city,
                    onValueChange = { onAction(EditPropertyAction.OnCityChanged(it)) },
                    label = "City / Area",
                    placeholder = "e.g. Kathmandu",
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = state.zipCode,
                    onValueChange = { onAction(EditPropertyAction.OnZipCodeChanged(it)) },
                    label = "Zip Code",
                    placeholder = "44600",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Type + Units
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
                        Icon(
                            Icons.Default.Category, contentDescription = null,
                            tint = iconTint, modifier = Modifier.size(18.dp)
                        )
                    }
                )
                AppTextField(
                    value = state.totalUnits,
                    onValueChange = { onAction(EditPropertyAction.OnTotalUnitsChanged(it)) },
                    label = "Total Units",
                    placeholder = "1",
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            Icons.Default.GridView, contentDescription = null,
                            tint = iconTint, modifier = Modifier.size(18.dp)
                        )
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
                    Icon(
                        Icons.Default.Event, contentDescription = null,
                        tint = iconTint, modifier = Modifier.size(20.dp)
                    )
                }
            )

            Spacer(Modifier.height(24.dp))

            // Amenities
            Text(
                text = "Amenities & Utilities Managed",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val amenities = listOf("Water", "Electricity", "Internet", "Trash")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in amenities.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EditAmenityChip(
                            label = amenities[i],
                            isSelected = state.selectedAmenities.contains(amenities[i]),
                            onToggle = { onAction(EditPropertyAction.OnAmenityToggled(amenities[i])) },
                            modifier = Modifier.weight(1f)
                        )
                        if (i + 1 < amenities.size) {
                            EditAmenityChip(
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
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
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
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Update Property",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
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
private fun EditAmenityChip(
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
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh),
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Edit Property — Filled")
@Composable
private fun EditPropertyFilledPreview() {
    RentManagerTheme {
        EditPropertyContent(
            state = EditPropertyState(
                name = "Peaceful Villa",
                streetAddress = "Koteshwor",
                city = "Kathmandu",
                zipCode = "44600",
                propertyType = "HOUSE",
                totalUnits = "4",
                selectedAmenities = setOf("Water", "Electricity"),
                isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Edit Property — Loading")
@Composable
private fun EditPropertyLoadingPreview() {
    RentManagerTheme {
        EditPropertyContent(
            state = EditPropertyState(isLoading = true),
            onAction = {}
        )
    }
}

@Preview(name = "Edit Property — Saving")
@Composable
private fun EditPropertySavingPreview() {
    RentManagerTheme {
        EditPropertyContent(
            state = EditPropertyState(
                name = "Peaceful Villa",
                streetAddress = "Koteshwor",
                city = "Kathmandu",
                propertyType = "HOUSE",
                isLoading = false,
                isSaving = true
            ),
            onAction = {}
        )
    }
}
