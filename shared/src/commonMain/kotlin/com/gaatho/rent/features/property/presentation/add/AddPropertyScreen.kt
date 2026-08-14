package com.gaatho.rent.features.property.presentation.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.core.ui.components.AppDocumentPicker
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppDropdownDefaults
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.features.property.presentation.edit.EditPropertyAction
import com.gaatho.rent.features.property.presentation.edit.EditPropertySideEffect
import com.gaatho.rent.features.property.presentation.edit.EditPropertyState
import com.gaatho.rent.features.property.presentation.edit.EditPropertyViewModel
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.add_property_title
import rentmanagerapp.shared.generated.resources.address_label
import rentmanagerapp.shared.generated.resources.address_placeholder
import rentmanagerapp.shared.generated.resources.city_label
import rentmanagerapp.shared.generated.resources.city_placeholder
import rentmanagerapp.shared.generated.resources.description_label
import rentmanagerapp.shared.generated.resources.description_placeholder
import rentmanagerapp.shared.generated.resources.monthly_rent_npr_label
import rentmanagerapp.shared.generated.resources.number_of_units_label
import rentmanagerapp.shared.generated.resources.property_name_label
import rentmanagerapp.shared.generated.resources.property_name_placeholder
import rentmanagerapp.shared.generated.resources.property_photos_label
import rentmanagerapp.shared.generated.resources.property_type_label
import rentmanagerapp.shared.generated.resources.property_type_placeholder
import rentmanagerapp.shared.generated.resources.rent_placeholder
import rentmanagerapp.shared.generated.resources.save_property_btn
import rentmanagerapp.shared.generated.resources.units_placeholder

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
        AppDialog(
            variant = AppDialog.Variant.Success,
            layout = AppDialog.Layout.Center,
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
            AppTopBar(
                title = stringResource(Res.string.add_property_title),
                onBackClick = { onAction(EditPropertyAction.OnBackClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        val labelStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
        val fieldStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp
        )
        val fieldShape = RoundedCornerShape(12.dp)
        val propertyTypes = persistentListOf("HOUSE", "APARTMENT", "FLAT", "SHOP", "BUILDING")
        val typeLabel: (String) -> String = { type ->
            type.lowercase().replaceFirstChar { it.uppercase() }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Property Name
                AppTextField(
                    value = state.name,
                    onValueChange = { onAction(EditPropertyAction.OnNameChanged(it)) },
                    label = stringResource(Res.string.property_name_label),
                    placeholder = stringResource(Res.string.property_name_placeholder),
                    errorMessage = state.nameError,
                    labelStyle = labelStyle,
                    fieldTextStyle = fieldStyle,
                    shape = fieldShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Address
                AppTextField(
                    value = state.streetAddress,
                    onValueChange = { onAction(EditPropertyAction.OnStreetAddressChanged(it)) },
                    label = stringResource(Res.string.address_label),
                    placeholder = stringResource(Res.string.address_placeholder),
                    errorMessage = state.addressError,
                    labelStyle = labelStyle,
                    fieldTextStyle = fieldStyle,
                    shape = fieldShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // City / Area
                AppTextField(
                    value = state.city,
                    onValueChange = { onAction(EditPropertyAction.OnCityChanged(it)) },
                    label = stringResource(Res.string.city_label),
                    placeholder = stringResource(Res.string.city_placeholder),
                    labelStyle = labelStyle,
                    fieldTextStyle = fieldStyle,
                    shape = fieldShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Property Type
                AppDropdown(
                    options = propertyTypes,
                    selectedItem = state.propertyType,
                    itemLabel = typeLabel,
                    onItemSelected = { onAction(EditPropertyAction.OnTypeChanged(it)) },
                    label = stringResource(Res.string.property_type_label),
                    placeholder = stringResource(Res.string.property_type_placeholder),
                    labelStyle = labelStyle,
                    fieldTextStyle = fieldStyle,
                    shape = fieldShape,
                    colors = AppDropdownDefaults.colors(iconColor = AppColors.EmeraldAccent),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Number of Units + Monthly Rent
                Row(
                    Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)
                ) {
                    ->
                    AppTextField(
                        value = state.totalUnits,
                        onValueChange = { onAction(EditPropertyAction.OnTotalUnitsChanged(it)) },
                        label = stringResource(Res.string.number_of_units_label),
                        placeholder = stringResource(Res.string.units_placeholder),
                        labelStyle = labelStyle,
                        fieldTextStyle = fieldStyle,
                        shape = fieldShape,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        value = state.monthlyRent,
                        onValueChange = { onAction(EditPropertyAction.OnMonthlyRentChanged(it)) },
                        label = stringResource(Res.string.monthly_rent_npr_label),
                        placeholder = stringResource(Res.string.rent_placeholder),
                        labelStyle = labelStyle,
                        fieldTextStyle = fieldStyle,
                        shape = fieldShape,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Description
                AppTextField(
                    value = state.description,
                    onValueChange = { onAction(EditPropertyAction.OnDescriptionChanged(it)) },
                    label = stringResource(Res.string.description_label),
                    placeholder = stringResource(Res.string.description_placeholder),
                    labelStyle = labelStyle,
                    fieldTextStyle = fieldStyle,
                    shape = fieldShape,
                    singleLine = false,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Property Photos
                AppDocumentPicker(
                    title = stringResource(Res.string.property_photos_label),
                    file = null,
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Save
                Button(
                    onClick = { onAction(EditPropertyAction.OnSaveClicked) },
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        disabledElevation = 0.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.save_property_btn),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
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
                name = TextFieldValue("Peaceful Villa"),
                streetAddress = TextFieldValue("Koteshwor"),
                city = TextFieldValue("Kathmandu"),
                propertyType = "HOUSE",
                totalUnits = TextFieldValue("4"),
                monthlyRent = TextFieldValue("25000"),
                isLoading = false,
                description = TextFieldValue("Parking available, water & electricity included.")
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
                name = TextFieldValue("Peaceful Villa"),
                isSaving = true
            ),
            onAction = {}
        )
    }
}
