package com.gaatho.rent.features.property.presentation.edit

import rentmanagerapp.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.collections.immutable.toPersistentList


import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.components.RentManagerOutlinedButton
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.ui.components.AppCard
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppImageSourcePicker
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppTopBar
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.cancel_action
import rentmanagerapp.shared.generated.resources.delete_action
import rentmanagerapp.shared.generated.resources.delete_property_desc
import rentmanagerapp.shared.generated.resources.delete_property_title
import rentmanagerapp.shared.generated.resources.property_city_label
import rentmanagerapp.shared.generated.resources.property_city_placeholder
import rentmanagerapp.shared.generated.resources.property_name_label
import rentmanagerapp.shared.generated.resources.property_name_placeholder
import rentmanagerapp.shared.generated.resources.property_street_label
import rentmanagerapp.shared.generated.resources.property_street_placeholder
import rentmanagerapp.shared.generated.resources.property_total_units_label
import rentmanagerapp.shared.generated.resources.property_total_units_placeholder
import rentmanagerapp.shared.generated.resources.property_type_label
import rentmanagerapp.shared.generated.resources.property_type_placeholder
import rentmanagerapp.shared.generated.resources.property_unit_name_indexed_label
import rentmanagerapp.shared.generated.resources.property_unit_name_label
import rentmanagerapp.shared.generated.resources.property_unit_name_placeholder
import rentmanagerapp.shared.generated.resources.property_unit_rent_label
import rentmanagerapp.shared.generated.resources.property_unit_rent_placeholder
import rentmanagerapp.shared.generated.resources.property_waste_charge_label
import rentmanagerapp.shared.generated.resources.property_waste_charge_placeholder

@Composable
fun EditPropertyScreen(
    propertyId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditPropertyViewModel = koinViewModel(parameters = { parametersOf(propertyId) })
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
                title = if (propertyId == "new") "Add New Property" else "Edit Property Details",
                onBackClick = { viewModel.onAction(EditPropertyAction.OnBackClicked) },
                containerColor = MaterialTheme.colorScheme.background
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PropertyFormContent(
            state = state,
            onAction = viewModel::onAction,
            isNewProperty = propertyId == "new",
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun PropertyFormContent(
    state: EditPropertyState,
    onAction: (EditPropertyAction) -> Unit,
    isNewProperty: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showImageSourcePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp)
                        .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    Spacer(Modifier.height(16.dp))

                    // ── Basic Info ──────────────────────────────────────────────────
                    AppTextField(
                        value = state.name,
                        onValueChange = { onAction(EditPropertyAction.OnNameChanged(it)) },
                        label = stringResource(Res.string.property_name_label),
                        placeholder = stringResource(Res.string.property_name_placeholder),
                        errorMessage = state.nameError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    AppTextField(
                        value = state.streetAddress,
                        onValueChange = { onAction(EditPropertyAction.OnStreetAddressChanged(it)) },
                        label = stringResource(Res.string.property_street_label),
                        placeholder = stringResource(Res.string.property_street_placeholder),
                        errorMessage = state.addressError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    AppTextField(
                        value = state.city,
                        onValueChange = { onAction(EditPropertyAction.OnCityChanged(it)) },
                        label = stringResource(Res.string.property_city_label),
                        placeholder = stringResource(Res.string.property_city_placeholder),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    AppDropdown(
                        options = state.availablePropertyTypes.toPersistentList(),
                        selectedItem = state.propertyType,
                        onItemSelected = { onAction(EditPropertyAction.OnTypeChanged(it)) },
                        itemLabel = { it.lowercase().replaceFirstChar { ch -> ch.uppercase() } },
                        label = stringResource(Res.string.property_type_label),
                        placeholder = stringResource(Res.string.property_type_placeholder),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Units Configuration ──────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppTextField(
                            value = state.totalUnits,
                            onValueChange = { onAction(EditPropertyAction.OnTotalUnitsChanged(it)) },
                            label = stringResource(Res.string.property_total_units_label),
                            placeholder = stringResource(Res.string.property_total_units_placeholder),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Unit Names & Rents Editor ────────────────────────────────────
                    if (state.unitsCount > 0) {
                        Text(state.unitSectionLabel, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))

                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)), 
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                state.units.forEachIndexed { index, unit ->
                                    key(unit.id.ifBlank { index }) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            AppTextField(
                                                value = unit.name,
                                                onValueChange = { onAction(EditPropertyAction.OnUnitNameChanged(index, it)) },
                                                label = if (state.unitsCount == 1) stringResource(Res.string.property_unit_name_label) else stringResource(Res.string.property_unit_name_indexed_label, index + 1),
                                                placeholder = stringResource(Res.string.property_unit_name_placeholder),
                                                modifier = Modifier.weight(1.5f)
                                            )
                                            AppTextField(
                                                value = unit.monthlyRent,
                                                onValueChange = { onAction(EditPropertyAction.OnUnitRentChanged(index, it)) },
                                                label = stringResource(Res.string.property_unit_rent_label),
                                                placeholder = stringResource(Res.string.property_unit_rent_placeholder),
                                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // ── Utility Charges ─────────────────────────────────────────────
                    Text("Monthly Utility Charges (Optional)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AppTextField(
                                    value = state.wifiCharge,
                                    onValueChange = { onAction(EditPropertyAction.OnWifiChargeChanged(it)) },
                                    label = "WiFi",
                                    placeholder = "e.g. 500",
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                AppTextField(
                                    value = state.waterCharge,
                                    onValueChange = { onAction(EditPropertyAction.OnWaterChargeChanged(it)) },
                                    label = "Water",
                                    placeholder = "e.g. 300",
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AppTextField(
                                    value = state.electricityCharge,
                                    onValueChange = { onAction(EditPropertyAction.OnElectricityChargeChanged(it)) },
                                    label = "Electricity",
                                    placeholder = "e.g. 0 (per unit)",
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                AppTextField(
                                    value = state.wasteCharge,
                                    onValueChange = { onAction(EditPropertyAction.OnWasteChargeChanged(it)) },
                                    label = stringResource(Res.string.property_waste_charge_label),
                                    placeholder = stringResource(Res.string.property_waste_charge_placeholder),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    PropertyPhotoSection(
                        imageUrl = state.imageUrl,
                        uploadedImageName = state.uploadedImageName,
                        previewBytes = state.pendingImageBytes,
                        isCompressing = state.isCompressingImage,
                        onImagePicked = { name, bytes -> onAction(EditPropertyAction.OnImagePicked(name, bytes)) },
                        showSourcePicker = showImageSourcePicker,
                        onShowSourcePickerChange = { showImageSourcePicker = it }
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        // ── Footer actions ──────────────────────────────────────────────────
        if (!state.isLoading) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isNewProperty) {
                        RentManagerOutlinedButton(
                            text = "Delete",
                            onClick = { onAction(EditPropertyAction.OnDeleteClicked) },
                            modifier = Modifier.weight(1f),
                            borderColor = AppColors.Error,
                            contentColor = AppColors.Error
                        )
                    }
                    RentManagerPrimaryButton(
                        text = if (isNewProperty) "Save Property" else "Update Changes",
                        onClick = { onAction(EditPropertyAction.OnSaveClicked) },
                        modifier = Modifier.weight(if (isNewProperty) 2f else 1f),
                        isLoading = state.isSaving
                    )
                }
            }
        }

        if (state.showDeleteConfirm) {
            AppDialog(
                icon = Icons.Default.Delete,
                title = stringResource(Res.string.delete_property_title),
                body = stringResource(Res.string.delete_property_desc),
                confirmText = stringResource(Res.string.delete_action),
                dismissText = stringResource(Res.string.cancel_action),
                onConfirm = { onAction(EditPropertyAction.OnDeleteConfirmed) },
                onDismiss = { onAction(EditPropertyAction.OnDeleteDismissed) },
                variant = AppDialog.Variant.Destructive
            )
        }

        if (state.showSuccessDialog) {
            AppDialog(
                icon = Icons.Default.Home,
                title = if (isNewProperty) "Property Added" else "Property Updated",
                body = if (isNewProperty) "The new property has been successfully added to your portfolio." else "The property details have been successfully updated.",
                confirmText = "Okay",
                dismissText = null,
                onConfirm = { onAction(EditPropertyAction.OnSuccessDialogDismissed) },
                onDismiss = { onAction(EditPropertyAction.OnSuccessDialogDismissed) },
                variant = AppDialog.Variant.Success
            )
        }
    }
}

@Composable
private fun PropertyPhotoSection(
    imageUrl: String?,
    uploadedImageName: String?,
    onImagePicked: (String, ByteArray) -> Unit,
    showSourcePicker: Boolean,
    onShowSourcePickerChange: (Boolean) -> Unit,
    previewBytes: ByteArray? = null,
    isCompressing: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val galleryLauncher = rememberFilePickerLauncher(type = FileKitType.Image, mode = FileKitMode.Single) { file ->
        if (file != null) scope.launch { onImagePicked(file.name, file.readBytes()) }
    }
    val cameraLauncher = rememberCameraPickerLauncher { file ->
        if (file != null) scope.launch { onImagePicked(file.name, file.readBytes()) }
    }

    if (showSourcePicker) {
        AppImageSourcePicker(
            onDismissRequest = { onShowSourcePickerChange(false) },
            onGalleryClick = { galleryLauncher.launch() },
            onCameraClick = { cameraLauncher.launch() }
        )
    }

    val shape = RoundedCornerShape(12.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Property Photo",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable { onShowSourcePickerChange(true) },
            contentAlignment = Alignment.Center
        ) {
            when {
                previewBytes != null -> {
                    // Live local preview before upload
                    com.gaatho.rent.core.ui.components.AppAsyncImage(
                        model = previewBytes,
                        contentDescription = "Property Image Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // "Tap to change" overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tap to change photo", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
                imageUrl != null -> {
                    com.gaatho.rent.core.ui.components.AppAsyncImage(
                        model = imageUrl,
                        contentDescription = "Property Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tap to change photo", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap to add property photo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (isCompressing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}


