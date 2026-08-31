package com.gaatho.rent.features.tenant.presentation.edit

import rentmanagerapp.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.collections.immutable.toPersistentList


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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.components.RentManagerOutlinedButton
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.ui.components.AppDatePickerDialog
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppImageSourcePicker
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.core.utils.DateTimeUtil
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import com.gaatho.rent.core.ui.components.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.cancel_action
import rentmanagerapp.shared.generated.resources.id_proof_upload_label
import rentmanagerapp.shared.generated.resources.remove_action
import rentmanagerapp.shared.generated.resources.remove_tenant_desc
import rentmanagerapp.shared.generated.resources.remove_tenant_title

@Composable
fun EditTenantScreen(
    tenantId: String,
    onNavigateBack: () -> Unit,
    onNavigateToTenantList: () -> Unit,
    viewModel: EditTenantViewModel = koinViewModel(parameters = { parametersOf(tenantId) })
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is EditTenantSideEffect.NavigateBack -> onNavigateBack()
            is EditTenantSideEffect.NavigateToTenantList -> onNavigateToTenantList()
            is EditTenantSideEffect.ShowSnackbar ->
                snackbarHostState.showSnackbar(effect.message)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.tenant_edit_title),
                onBackClick = { viewModel.onAction(EditTenantAction.OnBackClicked) },
                containerColor = MaterialTheme.colorScheme.background,
                titleStyle = MaterialTheme.typography.titleMedium
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RentManagerOutlinedButton(
                        text = stringResource(Res.string.tenant_remove_button),
                        onClick = { viewModel.onAction(EditTenantAction.OnDeleteClicked) },
                        modifier = Modifier.weight(1f),
                        borderColor = AppColors.Error,
                        contentColor = AppColors.Error
                    )
                    RentManagerPrimaryButton(
                        text = stringResource(Res.string.tenant_save_changes_button),
                        onClick = { viewModel.onAction(EditTenantAction.OnSaveClicked) },
                        modifier = Modifier.weight(1f),
                        isLoading = state.isSaving
                    )
                }
            }
        },
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
fun EditTenantContent(
    state: EditTenantState,
    onAction: (EditTenantAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.showSuccessDialog) {
        AppDialog(
            variant = AppDialog.Variant.Success,
            layout = AppDialog.Layout.Center,
            icon = Icons.Default.CheckCircle,
            title = stringResource(Res.string.tenant_success_title),
            body = stringResource(Res.string.tenant_saved_body),
            confirmText = stringResource(Res.string.common_ok),
            onConfirm = { onAction(EditTenantAction.OnSuccessDialogDismissed) },
            onDismiss = { onAction(EditTenantAction.OnSuccessDialogDismissed) }
        )
    }

    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showProfileSourcePicker by remember { mutableStateOf(false) }
    var showDocumentSourcePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        AppDatePickerDialog(
            selectedDate = state.moveInDate,
            onDateSelected = {
                onAction(EditTenantAction.OnMoveInDateChanged(it))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            title = stringResource(Res.string.tenant_move_in_date_label)
        )
    }

    val scope = rememberCoroutineScope()

    // --- Gallery Launchers ---
    val documentGalleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { file ->
        if (file != null) {
            scope.launch {
                val bytes = file.readBytes()
                onAction(EditTenantAction.OnDocumentPicked(file.name, bytes))
            }
        }
    }

    val profileGalleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { file ->
        if (file != null) {
            scope.launch {
                val bytes = file.readBytes()
                onAction(EditTenantAction.OnProfileImagePicked(file.name, bytes))
            }
        }
    }

    // --- Camera Launchers ---
    val documentCameraLauncher = rememberCameraPickerLauncher { file ->
        if (file != null) {
            scope.launch {
                val bytes = file.readBytes()
                onAction(EditTenantAction.OnDocumentPicked(file.name, bytes))
            }
        }
    }

    val profileCameraLauncher = rememberCameraPickerLauncher { file ->
        if (file != null) {
            scope.launch {
                val bytes = file.readBytes()
                onAction(EditTenantAction.OnProfileImagePicked(file.name, bytes))
            }
        }
    }

    // --- Source Pickers ---
    if (showProfileSourcePicker) {
        AppImageSourcePicker(
            onDismissRequest = { showProfileSourcePicker = false },
            onGalleryClick = { profileGalleryLauncher.launch() },
            onCameraClick = { profileCameraLauncher.launch() }
        )
    }

    if (showDocumentSourcePicker) {
        AppImageSourcePicker(
            onDismissRequest = { showDocumentSourcePicker = false },
            onGalleryClick = { documentGalleryLauncher.launch() },
            onCameraClick = { documentCameraLauncher.launch() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.propertyOptions.isEmpty()) {
            NoPropertiesEmptyState(modifier = Modifier.fillMaxSize())
        } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Profile Photo ────────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clickable { showProfileSourcePicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    com.gaatho.rent.core.ui.components.AppAsyncImage(
                        model = state.pendingProfileBytes ?: state.profileImageUrl,
                        contentDescription = "Profile Photo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        placeholderType = com.gaatho.rent.core.ui.components.PlaceholderType.AVATAR
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            AppTextField(
                value = state.name,
                onValueChange = { onAction(EditTenantAction.OnNameChanged(it)) },
                label = stringResource(Res.string.tenant_full_name_label),
                placeholder = stringResource(Res.string.tenant_full_name_placeholder),
                errorMessage = state.nameError,



                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.phone,
                onValueChange = { onAction(EditTenantAction.OnPhoneChanged(it)) },
                label = stringResource(Res.string.tenant_phone_label),
                placeholder = stringResource(Res.string.tenant_phone_placeholder),



                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.email,
                onValueChange = { onAction(EditTenantAction.OnEmailChanged(it)) },
                label = stringResource(Res.string.tenant_email_label),
                placeholder = stringResource(Res.string.tenant_email_placeholder),



                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            AppDropdown(
                options = state.propertyNames.toPersistentList(),
                selectedItem = state.selectedPropertyName,
                onItemSelected = { name ->
                    val id = state.propertyOptions.find { it.name == name }?.id
                    if (id != null) onAction(EditTenantAction.OnPropertySelected(id))
                },
                label = stringResource(Res.string.assign_property_label),
                placeholder = stringResource(Res.string.assign_property_placeholder),



                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.unitOptions.isNotEmpty()) {
                    AppDropdown(
                        options = state.unitOptions.toPersistentList(),
                        selectedItem = if (state.unitNumber.text.isNotBlank()) state.unitNumber.text else null,
                        onItemSelected = { onAction(EditTenantAction.OnUnitSelected(it)) },
                        label = stringResource(Res.string.unit_number_label),
                        placeholder = stringResource(Res.string.tenant_unit_number_placeholder),



                        modifier = Modifier.weight(1f)
                    )
                } else {
                    AppTextField(
                        value = state.unitNumber,
                        onValueChange = { onAction(EditTenantAction.OnUnitNumberChanged(it)) },
                        label = stringResource(Res.string.unit_number_label),
                        placeholder = stringResource(Res.string.tenant_unit_number_placeholder),



                        modifier = Modifier.weight(1f)
                    )
                }
                AppTextField(
                    value = state.rentAmount,
                    onValueChange = { onAction(EditTenantAction.OnRentChanged(it)) },
                    label = stringResource(Res.string.tenant_rent_amount_label),
                    placeholder = stringResource(Res.string.tenant_rent_amount_placeholder),
                    errorMessage = state.rentError,



                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(
                        value = DateTimeUtil.formatDisplayDate(state.moveInDate),
                        onValueChange = {},
                        readOnly = true,
                        label = stringResource(Res.string.tenant_move_in_date_label),
                        placeholder = stringResource(Res.string.tenant_move_in_date_placeholder),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                AppDropdown(
                    options = state.availableLeaseDurations.toPersistentList(),
                    selectedItem = state.leaseDuration,
                    onItemSelected = { onAction(EditTenantAction.OnLeaseDurationSelected(it)) },
                    label = stringResource(Res.string.lease_duration_label),
                    placeholder = stringResource(Res.string.lease_duration_placeholder),



                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = state.securityDeposit,
                onValueChange = { onAction(EditTenantAction.OnSecurityDepositChanged(it)) },
                label = stringResource(Res.string.tenant_security_deposit_npr_label),
                placeholder = stringResource(Res.string.tenant_security_deposit_placeholder),



                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // ── Utilities Toggles ───────────────────────────────────────────
            BodyText(
                text = stringResource(Res.string.utilities_included_label),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            com.gaatho.rent.core.ui.components.AppCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BodyText(state.wifiLabel)
                        androidx.compose.material3.Switch(checked = state.hasWifi, onCheckedChange = { onAction(EditTenantAction.OnWifiToggled(it)) })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BodyText(state.waterLabel)
                        androidx.compose.material3.Switch(checked = state.hasWater, onCheckedChange = { onAction(EditTenantAction.OnWaterToggled(it)) })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BodyText(state.electricityLabel)
                        androidx.compose.material3.Switch(checked = state.hasElectricity, onCheckedChange = { onAction(EditTenantAction.OnElectricityToggled(it)) })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BodyText(state.wasteLabel)
                        androidx.compose.material3.Switch(checked = state.hasWaste, onCheckedChange = { onAction(EditTenantAction.OnWasteToggled(it)) })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Document Type Dropdown ───────────────────────────────────────
            AppDropdown(
                options = state.availableDocumentTypes.toPersistentList(),
                selectedItem = state.documentType,
                onItemSelected = { onAction(EditTenantAction.OnDocumentTypeSelected(it)) },
                label = stringResource(Res.string.document_type_label),
                placeholder = stringResource(Res.string.document_type_placeholder),

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            com.gaatho.rent.core.ui.components.AppDocumentPicker(
                title = stringResource(Res.string.id_proof_upload_label),
                file = state.documentUrl,
                previewBytes = state.pendingDocBytes,
                onClick = { showDocumentSourcePicker = true },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            
            com.gaatho.rent.core.ui.components.AppCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    AnimatedPriceLabel(
                        priceText = if (state.rentAmount.text.isNotBlank()) state.rentAmount.text else "0",
                        label = stringResource(Res.string.tenant_total_monthly_due)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
        }
    }

    if (state.showDeleteConfirm) {
        com.gaatho.rent.core.ui.components.AppDialog(
            icon = Icons.Default.Delete,
            title = stringResource(Res.string.remove_tenant_title),
            body = stringResource(Res.string.remove_tenant_desc),
            confirmText = stringResource(Res.string.remove_action),
            dismissText = stringResource(Res.string.cancel_action),
            onConfirm = { onAction(EditTenantAction.OnDeleteConfirmed) },
            onDismiss = { onAction(EditTenantAction.OnDeleteDismissed) },
            variant = com.gaatho.rent.core.ui.components.AppDialog.Variant.Destructive
        )
    }
}

@Composable
private fun AnimatedPriceLabel(
    priceText: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CardTitle(
            text = label,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            CardTitle(
                text = stringResource(Res.string.currency_npr) + " ",
                color = MaterialTheme.colorScheme.primary
            )
            AnimatedContent(
                targetState = priceText,
                transitionSpec = {
                    val targetNum = targetState.toLongOrNull() ?: 0L
                    val initialNum = initialState.toLongOrNull() ?: 0L
                    if (targetNum > initialNum) {
                        (slideInVertically { height -> height } + fadeIn())
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                    } else {
                        (slideInVertically { height -> -height } + fadeIn())
                            .togetherWith(slideOutVertically { height -> height } + fadeOut())
                    }
                },
                label = "price_animation"
            ) { targetText ->
                SectionTitle(
                    text = targetText,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Edit Tenant — Filled")
@Composable
private fun EditTenantFilledPreview() {
    RentManagerTheme {
        EditTenantContent(
            state = EditTenantState(
                name = TextFieldValue("Suman Maharjan"),
                phone = TextFieldValue("9841876543"),
                email = TextFieldValue("suman.maharjan@gmail.com"),
                rentAmount = TextFieldValue("25000"),
                unitNumber = TextFieldValue("Unit 2B"),
                moveInDate = "2023-10-18",
                leaseDuration = "1 Year",
                securityDeposit = TextFieldValue("50000"),
                isLoading = false,
                propertyId = "prop-1",
                propertyOptions = listOf(PropertyOption("prop-1", "Baluwatar House"))
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Edit Tenant — Loading")
@Composable
private fun EditTenantLoadingPreview() {
    RentManagerTheme {
        EditTenantContent(
            state = EditTenantState(isLoading = true),
            onAction = {}
        )
    }
}

@Composable
private fun NoPropertiesEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(24.dp))
        SectionTitle(
            text = stringResource(Res.string.no_properties_empty_title),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        BodyText(
            text = stringResource(Res.string.no_properties_empty_body),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}





