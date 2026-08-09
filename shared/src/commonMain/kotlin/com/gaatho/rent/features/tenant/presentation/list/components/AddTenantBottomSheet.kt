package com.gaatho.rent.features.tenant.presentation.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppSelectionBottomSheet
import com.gaatho.rent.core.ui.components.AppSelectionItem
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppDatePickerDialog
import com.gaatho.rent.features.tenant.presentation.add.AddTenantAction
import com.gaatho.rent.features.tenant.presentation.add.AddTenantState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantBottomSheet(
    state: AddTenantState,
    onAction: (AddTenantAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val scrollState = rememberScrollState()
    
    var showPropertySheet by remember { mutableStateOf(false) }



    // Date Picker Dialog
    if (state.showDatePicker) {
        AppDatePickerDialog(
            selectedDate = if (state.isSelectingStartDate) state.startDate else state.endDate,
            onDateSelected = { onAction(AddTenantAction.OnDateSelected(it)) },
            onDismiss = { onAction(AddTenantAction.OnDatePickerDismissed) },
            title = if (state.isSelectingStartDate) "Select Start Date" else "Select End Date"
        )
    }

    // Property Selection Bottom Sheet
    if (showPropertySheet) {
        val properties = (state.propertiesState as? UiState.Success)?.data ?: kotlinx.collections.immutable.persistentListOf()
        AppSelectionBottomSheet(
            title = "Select Property",
            items = properties.map { AppSelectionItem(it.id, it.name) },
            selectedId = state.selectedPropertyId,
            onItemSelected = { 
                onAction(AddTenantAction.OnPropertySelected(it))
                showPropertySheet = false 
            },
            onDismiss = { showPropertySheet = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding() // Automatically pushes the entire sheet up when keyboard appears
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Drag Handle and Title Header
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
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.add_tenant),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // --- Photo Section with Dashed Border ---
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val outlineColor = MaterialTheme.colorScheme.outlineVariant
                Box(
                    modifier = Modifier.size(86.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = outlineColor,
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )
                    }
                    
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = stringResource(Res.string.add_photo),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(Res.string.add_photo),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Form Fields ---
            AppTextField(
                value = state.fullName,
                onValueChange = { onAction(AddTenantAction.OnFullNameChanged(it)) },
                label = stringResource(Res.string.tenant_name_label),
                placeholder = "e.g. Ram Bahadur Thapa",
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = state.phone,
                onValueChange = { onAction(AddTenantAction.OnPhoneChanged(it)) },
                label = stringResource(Res.string.tenant_phone_label),
                placeholder = "98XXXXXXXX",
                prefix = "+977",
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = state.email,
                onValueChange = { onAction(AddTenantAction.OnEmailChanged(it)) },
                label = stringResource(Res.string.tenant_email_label),
                topRightLabel = stringResource(Res.string.optional),
                placeholder = "ram@example.com",
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = state.address,
                onValueChange = { onAction(AddTenantAction.OnAddressChanged(it)) },
                label = stringResource(Res.string.tenant_address_label),
                placeholder = "e.g. Pokhara-8, Kaski",
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = state.occupation,
                onValueChange = { onAction(AddTenantAction.OnOccupationChanged(it)) },
                label = stringResource(Res.string.tenant_occupation_label),
                placeholder = "e.g. Software Engineer",
                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(24.dp))

            val properties = (state.propertiesState as? UiState.Success)?.data
            val propertyName = properties?.find { it.id == state.selectedPropertyId }?.name ?: ""
            Box(modifier = Modifier.fillMaxWidth().clickable { showPropertySheet = true }) {
                AppTextField(
                    value = propertyName,
                    onValueChange = {},
                    label = "Property (Required)",
                    placeholder = "Select a property...",
                    readOnly = true,
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.Domain, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) },
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AppTextField(
                value = state.roomNumber,
                onValueChange = { onAction(AddTenantAction.OnRoomNumberChanged(it)) },
                label = stringResource(Res.string.tenant_room_label),
                placeholder = "e.g. Room 101",
                leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f).clickable { onAction(AddTenantAction.OnDateFieldClicked(isStartDate = true)) }) {
                    AppTextField(
                        value = state.startDate,
                        onValueChange = {},
                        label = stringResource(Res.string.tenant_start_date_label),
                        placeholder = "dd-mm-yyyy",
                        readOnly = true,
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp)) }
                    )
                }

                Box(modifier = Modifier.weight(1f).clickable { onAction(AddTenantAction.OnDateFieldClicked(isStartDate = false)) }) {
                    AppTextField(
                        value = state.endDate,
                        onValueChange = {},
                        label = stringResource(Res.string.tenant_end_date_label),
                        topRightLabel = stringResource(Res.string.optional),
                        placeholder = "dd-mm-yyyy",
                        readOnly = true,
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            AppTextField(
                value = state.rentAmount,
                onValueChange = { onAction(AddTenantAction.OnRentAmountChanged(it)) },
                label = "Monthly Rent",
                placeholder = "0.00",
                prefix = "Rs.",
                leadingIcon = { Icon(Icons.Default.Money, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = state.deposit,
                onValueChange = { onAction(AddTenantAction.OnDepositChanged(it)) },
                label = stringResource(Res.string.tenant_deposit_label),
                placeholder = "0.00",
                prefix = "Rs.",
                leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Footer - Sticky
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Button(
                        onClick = { onAction(AddTenantAction.OnSaveClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = state.canSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(Res.string.tenant_add_button).uppercase(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
