package com.gaatho.rent.features.property.presentation.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.components.RentManagerOutlinedButton
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppTopBar
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ─── Entry point ──────────────────────────────────────────────────────────────

@Composable
fun EditPropertyScreen(
    propertyId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditPropertyViewModel = koinInject(parameters = { parametersOf(propertyId) })
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
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
            AppTopBar(
                title = "Edit Property",
                onBackClick = { viewModel.onAction(EditPropertyAction.OnBackClicked) },
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
        EditPropertyContent(
            state = state,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────

@Composable
private fun EditPropertyContent(
    state: EditPropertyState,
    onAction: (EditPropertyAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val labelStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
    val fieldStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    )
    val fieldShape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            return@Column
        }

        // ── Scrollable form ─────────────────────────────────────────────────
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
                onValueChange = { onAction(EditPropertyAction.OnNameChanged(it)) },
                label = "Property Name",
                placeholder = "e.g. Baluwatar House",
                errorMessage = state.nameError,
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.streetAddress,
                onValueChange = { onAction(EditPropertyAction.OnStreetAddressChanged(it)) },
                label = "Address",
                placeholder = "e.g. Ward No. 4, Baluwatar",
                errorMessage = state.addressError,
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.city,
                onValueChange = { onAction(EditPropertyAction.OnCityChanged(it)) },
                label = "City",
                placeholder = "e.g. Kathmandu",
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            val propertyTypes = persistentListOf("HOUSE", "APARTMENT", "FLAT", "SHOP", "BUILDING")
            AppDropdown(
                options = propertyTypes,
                selectedItem = state.propertyType,
                onItemSelected = { onAction(EditPropertyAction.OnTypeChanged(it)) },
                itemLabel = { it.lowercase().replaceFirstChar { ch -> ch.uppercase() } },
                label = "Property Type",
                placeholder = "Apartment",
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Number of Units + Monthly Rent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = state.totalUnits,
                    onValueChange = { onAction(EditPropertyAction.OnTotalUnitsChanged(it)) },
                    label = "Number of Units",
                    placeholder = "e.g. 5",
                    labelStyle = labelStyle,
                    fieldTextStyle = fieldStyle,
                    shape = fieldShape,
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = state.monthlyRent,
                    onValueChange = { onAction(EditPropertyAction.OnMonthlyRentChanged(it)) },
                    label = "Monthly Rent (NPR)",
                    placeholder = "e.g. 1,25,000",
                    labelStyle = labelStyle,
                    fieldTextStyle = fieldStyle,
                    shape = fieldShape,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.description,
                onValueChange = { onAction(EditPropertyAction.OnDescriptionChanged(it)) },
                label = "Description",
                placeholder = "e.g. 5 Units beautiful apartment block with secure parking...",
                singleLine = false,
                minLines = 3,
                labelStyle = labelStyle,
                fieldTextStyle = fieldStyle,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            PropertyPhotoSection()

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
                    text = "Delete",
                    onClick = { onAction(EditPropertyAction.OnDeleteClicked) },
                    modifier = Modifier.weight(1f),
                    borderColor = AppColors.Error,
                    contentColor = AppColors.Error
                )
                RentManagerPrimaryButton(
                    text = "Save Changes",
                    onClick = { onAction(EditPropertyAction.OnSaveClicked) },
                    modifier = Modifier.weight(1f),
                    isLoading = state.isSaving
                )
            }
        }
    }

    if (state.showDeleteConfirm) {
        com.gaatho.rent.core.ui.components.AppDialog(
            icon = Icons.Default.Delete,
            title = stringResource(Res.string.delete_property_title),
            body = stringResource(Res.string.delete_property_desc),
            confirmText = stringResource(Res.string.delete_action),
            dismissText = stringResource(Res.string.cancel_action),
            onConfirm = { onAction(EditPropertyAction.OnDeleteConfirmed) },
            onDismiss = { onAction(EditPropertyAction.OnDeleteDismissed) },
            variant = com.gaatho.rent.core.ui.components.AppDialog.Variant.Destructive
        )
    }
}

// ─── Property Photo ───────────────────────────────────────────────────────────

@Composable
private fun PropertyPhotoSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Property Photo",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Dotted "Change" box
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
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
                name = TextFieldValue("Baluwatar House"),
                streetAddress = TextFieldValue("Ward No. 4, Baluwatar"),
                city = TextFieldValue("Kathmandu"),
                propertyType = "APARTMENT",
                totalUnits = TextFieldValue("5"),
                monthlyRent = TextFieldValue("125000"),
                description = TextFieldValue("5 Units beautiful apartment block with secure parking."),
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
