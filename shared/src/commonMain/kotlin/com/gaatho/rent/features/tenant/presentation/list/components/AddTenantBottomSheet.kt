package com.gaatho.rent.features.tenant.presentation.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppModalBottomSheet
import com.gaatho.rent.core.ui.components.AppTextField
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AddTenantBottomSheet(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val scrollState = rememberScrollState()

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
                    text = "Add Tenant",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                                contentDescription = "Add Photo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add Photo",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Form Fields ---
            AppTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                placeholder = "e.g. Ram Bahadur Thapa",
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number",
                placeholder = "98XXXXXXXX",
                prefix = "+977",
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                topRightLabel = "Optional",
                placeholder = "ram@example.com",
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = address,
                onValueChange = { address = it },
                label = "Permanent Address",
                placeholder = "e.g. Pokhara-8, Kaski",
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = occupation,
                onValueChange = { occupation = it },
                label = "Occupation",
                placeholder = "e.g. Software Engineer",
                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(24.dp))

            val roomOptions = persistentListOf("Room 101", "Room 102", "Flat A", "Flat B", "Shop 1")
            AppDropdown(
                options = roomOptions,
                selectedItem = room.ifEmpty { null },
                onItemSelected = { room = it },
                label = "Select Room/Unit",
                placeholder = "Select a vacant room...",
                leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = "Start Date",
                    placeholder = "dd-mm-yyyy",
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp)) },
                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp)) }
                )

                AppTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = "End Date",
                    topRightLabel = "Optional",
                    placeholder = "dd-mm-yyyy",
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp)) },
                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = deposit,
                onValueChange = { deposit = it },
                label = "Security Deposit",
                placeholder = "0.00",
                prefix = "Rs.",
                leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Footer - Sticky
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
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
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Cancel", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1.5f).height(AppDimensions.ButtonHeightMedium),
                    shape = RoundedCornerShape(AppDimensions.RadiusPill),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Add Tenant", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Preview
@Composable
private fun AddTenantBottomSheetPreview() {
    RentManagerTheme {
        AddTenantBottomSheet(
            onDismiss = {},
            onSave = {}
        )
    }
}
