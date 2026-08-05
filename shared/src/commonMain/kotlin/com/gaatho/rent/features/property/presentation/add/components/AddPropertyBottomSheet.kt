package com.gaatho.rent.features.property.presentation.add.components

import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.features.property.presentation.add.AddPropertyAction
import com.gaatho.rent.features.property.presentation.add.AddPropertyState
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import com.gaatho.rent.core.utils.toImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun AddPropertyBottomSheet(
    state: AddPropertyState,
    onAction: (AddPropertyAction) -> Unit,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Add Property",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Photo Section
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(90.dp).clickable { onPickImage() },
                    contentAlignment = Alignment.Center
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = if (state.imageBytes != null) primaryColor else Color.LightGray,
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(82.dp)
                    ) {
                        if (state.imageBytes != null) {
                            val bitmap = remember(state.imageBytes) { state.imageBytes.toImageBitmap() }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Selected image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Added", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add Photo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = iconTint
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Optional: Help identify this property quickly.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            AppTextField(
                value = state.name,
                onValueChange = { onAction(AddPropertyAction.OnNameChanged(it)) },
                label = "Property Name",
                placeholder = "e.g. Sunset Residency",
                errorMessage = state.nameError,
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = state.streetAddress,
                onValueChange = { onAction(AddPropertyAction.OnStreetAddressChanged(it)) },
                label = "Street Address",
                placeholder = "e.g. 123 Main St",
                errorMessage = state.addressError,
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = state.city,
                onValueChange = { onAction(AddPropertyAction.OnCityChanged(it)) },
                label = "City / Area",
                placeholder = "e.g. Kathmandu",
                errorMessage = state.cityError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val propertyTypes =
                    persistentListOf("HOUSE", "APARTMENT", "FLAT", "SHOP", "BUILDING")
                AppDropdown(
                    options = propertyTypes,
                    selectedItem = state.propertyType,
                    onItemSelected = { onAction(AddPropertyAction.OnTypeChanged(it)) },
                    label = "Type",
                    placeholder = "Select...",
                    modifier = Modifier.weight(1.2f),
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp)) }
                )
                AppTextField(
                    value = state.totalUnits,
                    onValueChange = { onAction(AddPropertyAction.OnTotalUnitsChanged(it)) },
                    label = "Number of Units (Rooms/Flats)",
                    placeholder = "1",
                    errorMessage = state.unitsError,
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.GridView, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val billingCycles = persistentListOf("1st of the month", "15th of the month", "Last day of the month")
            AppDropdown(
                options = billingCycles,
                selectedItem = state.billingCycle,
                onItemSelected = { onAction(AddPropertyAction.OnBillingCycleChanged(it)) },
                label = "Billing Cycle",
                placeholder = "1st of the month",
                leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
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
                            onToggle = { onAction(AddPropertyAction.OnAmenityToggled(amenities[i])) },
                            modifier = Modifier.weight(1f)
                        )
                        if (i + 1 < amenities.size) {
                            AmenityChip(
                                label = amenities[i+1],
                                isSelected = state.selectedAmenities.contains(amenities[i+1]),
                                onToggle = { onAction(AddPropertyAction.OnAmenityToggled(amenities[i+1])) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Footer
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(AppDimensions.ButtonHeightMedium),
                    shape = RoundedCornerShape(AppDimensions.RadiusPill),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text(stringResource(Res.string.cancel), style = MaterialTheme.typography.titleMedium)
                }

                com.gaatho.rent.core.designsystem.components.RentManagerButton(
                    onClick = { onAction(AddPropertyAction.OnSaveClicked) },
                    modifier = Modifier.weight(1.5f),
                    enabled = state.name.isNotBlank() && state.streetAddress.isNotBlank() && state.city.isNotBlank() && state.totalUnits.isNotBlank() && !state.isSaving
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(stringResource(Res.string.property_create_button), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            }
        }
    }
}

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
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
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
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(4.dp)
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
