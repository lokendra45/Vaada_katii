package com.gaatho.rent.features.tenant.presentation.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.components.RentManagerOutlinedButton
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.ui.components.AppAsyncImage
import com.gaatho.rent.core.ui.components.AppCard
import com.gaatho.rent.core.ui.components.AppDatePickerDialog
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.core.ui.components.AppDocumentPicker
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppImageSourcePicker
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.core.ui.components.BodyText
import com.gaatho.rent.core.ui.components.CardTitle
import com.gaatho.rent.core.ui.components.PlaceholderType
import com.gaatho.rent.core.ui.components.SectionTitle
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.utils.ValidationUtil
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
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.assign_property_label
import rentmanagerapp.shared.generated.resources.assign_property_placeholder
import rentmanagerapp.shared.generated.resources.cancel_action
import rentmanagerapp.shared.generated.resources.common_ok
import rentmanagerapp.shared.generated.resources.currency_npr
import rentmanagerapp.shared.generated.resources.document_type_label
import rentmanagerapp.shared.generated.resources.document_type_placeholder
import rentmanagerapp.shared.generated.resources.id_proof_upload_label
import rentmanagerapp.shared.generated.resources.lease_duration_label
import rentmanagerapp.shared.generated.resources.lease_duration_placeholder
import rentmanagerapp.shared.generated.resources.no_properties_empty_body
import rentmanagerapp.shared.generated.resources.no_properties_empty_title
import rentmanagerapp.shared.generated.resources.remove_action
import rentmanagerapp.shared.generated.resources.remove_tenant_desc
import rentmanagerapp.shared.generated.resources.remove_tenant_title
import rentmanagerapp.shared.generated.resources.section_deposit_utilities
import rentmanagerapp.shared.generated.resources.section_documents
import rentmanagerapp.shared.generated.resources.section_lease_property
import rentmanagerapp.shared.generated.resources.section_personal_info
import rentmanagerapp.shared.generated.resources.tenant_edit_title
import rentmanagerapp.shared.generated.resources.tenant_email_label
import rentmanagerapp.shared.generated.resources.tenant_email_placeholder
import rentmanagerapp.shared.generated.resources.tenant_full_name_label
import rentmanagerapp.shared.generated.resources.tenant_full_name_placeholder
import rentmanagerapp.shared.generated.resources.tenant_move_in_date_label
import rentmanagerapp.shared.generated.resources.tenant_move_in_date_placeholder
import rentmanagerapp.shared.generated.resources.tenant_phone_label
import rentmanagerapp.shared.generated.resources.tenant_phone_placeholder
import rentmanagerapp.shared.generated.resources.tenant_remove_button
import rentmanagerapp.shared.generated.resources.tenant_rent_amount_label
import rentmanagerapp.shared.generated.resources.tenant_rent_amount_placeholder
import rentmanagerapp.shared.generated.resources.tenant_save_changes_button
import rentmanagerapp.shared.generated.resources.tenant_saved_body
import rentmanagerapp.shared.generated.resources.tenant_security_deposit_npr_label
import rentmanagerapp.shared.generated.resources.tenant_security_deposit_placeholder
import rentmanagerapp.shared.generated.resources.tenant_success_title
import rentmanagerapp.shared.generated.resources.tenant_total_monthly_due
import rentmanagerapp.shared.generated.resources.tenant_unit_number_placeholder
import rentmanagerapp.shared.generated.resources.unit_number_label
import rentmanagerapp.shared.generated.resources.utilities_included_label

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
                        isLoading = state.isSaving,
                        enabled = state.isFormValid && !state.isSaving
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

    // Section 1: Personal, 2: Lease, 3: Deposit/Utilities, 4: Documents (Manual toggle)
    var expandedSection by remember { mutableStateOf(1) }

    val isSec1Complete = state.name.text.isNotBlank() &&
            state.phone.text.isNotBlank() &&
            ValidationUtil.isValidNepaliPhone(state.phone.text) &&
            (state.email.text.isBlank() || ValidationUtil.isValidEmail(state.email.text))

    val isSec2Complete = !state.propertyId.isNullOrBlank() &&
            state.unitNumber.text.isNotBlank() &&
            state.rentAmount.text.isNotBlank() &&
            (state.rentAmount.text.toLongOrNull() ?: 0L) > 0L &&
            state.moveInDate.isNotBlank() &&
            state.leaseDuration.isNotBlank()

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
                    .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.8f))
            ) {
                Spacer(Modifier.height(16.dp))

                // ── Section 1: Personal Information ─────────────────────────
                val sec1Subtitle = listOf(state.name.text.trim(), state.phone.text.trim())
                    .filter { it.isNotBlank() }
                    .joinToString(" • ")
                    .ifBlank { null }

                FormSectionCard(
                    title = stringResource(Res.string.section_personal_info),
                    subtitle = sec1Subtitle,
                    icon = Icons.Default.Person,
                    isExpanded = expandedSection == 1,
                    isCompleted = isSec1Complete,
                    onHeaderClick = { expandedSection = if (expandedSection == 1) 0 else 1 }
                ) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .clickable { showProfileSourcePicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            AppAsyncImage(
                                model = state.pendingProfileBytes ?: state.profileImageUrl,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                placeholderType = PlaceholderType.AVATAR
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    AppTextField(
                        value = state.name,
                        onValueChange = { onAction(EditTenantAction.OnNameChanged(it)) },
                        label = stringResource(Res.string.tenant_full_name_label),
                        placeholder = stringResource(Res.string.tenant_full_name_placeholder),
                        errorMessage = state.nameError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(14.dp))

                    AppTextField(
                        value = state.phone,
                        onValueChange = { onAction(EditTenantAction.OnPhoneChanged(it)) },
                        label = stringResource(Res.string.tenant_phone_label),
                        placeholder = stringResource(Res.string.tenant_phone_placeholder),
                        errorMessage = state.phoneError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(14.dp))

                    AppTextField(
                        value = state.email,
                        onValueChange = { onAction(EditTenantAction.OnEmailChanged(it)) },
                        label = stringResource(Res.string.tenant_email_label),
                        placeholder = stringResource(Res.string.tenant_email_placeholder),
                        errorMessage = state.emailError,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── Section 2: Lease & Property Details ─────────────────────
                val sec2Subtitle = listOfNotNull(
                    state.selectedPropertyName,
                    state.unitNumber.text.takeIf { it.isNotBlank() },
                    if (state.rentAmount.text.isNotBlank()) "NPR ${state.rentAmount.text}" else null
                ).joinToString(" • ").ifBlank { null }

                FormSectionCard(
                    title = stringResource(Res.string.section_lease_property),
                    subtitle = sec2Subtitle,
                    icon = Icons.Default.Home,
                    isExpanded = expandedSection == 2,
                    isCompleted = isSec2Complete,
                    onHeaderClick = { expandedSection = if (expandedSection == 2) 0 else 2 }
                ) {
                    Spacer(Modifier.height(8.dp))

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

                    Spacer(Modifier.height(14.dp))

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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

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
                }

                Spacer(Modifier.height(12.dp))

                // ── Section 3: Deposit & Utilities (Optional) ───────────────
                val activeUtilsCount = listOf(state.hasWifi, state.hasWater, state.hasElectricity, state.hasWaste).count { it }
                val isSec3Complete = state.securityDeposit.text.isNotBlank() || activeUtilsCount > 0
                val sec3Subtitle = listOfNotNull(
                    if (state.securityDeposit.text.isNotBlank()) "Deposit: NPR ${state.securityDeposit.text}" else null,
                    if (activeUtilsCount > 0) "$activeUtilsCount Utilities Included" else null
                ).joinToString(" • ").ifBlank { null }

                FormSectionCard(
                    title = stringResource(Res.string.section_deposit_utilities),
                    subtitle = sec3Subtitle,
                    icon = Icons.Default.Payments,
                    isExpanded = expandedSection == 3,
                    isCompleted = isSec3Complete,
                    onHeaderClick = { expandedSection = if (expandedSection == 3) 0 else 3 }
                ) {
                    Spacer(Modifier.height(8.dp))

                    AppTextField(
                        value = state.securityDeposit,
                        onValueChange = { onAction(EditTenantAction.OnSecurityDepositChanged(it)) },
                        label = stringResource(Res.string.tenant_security_deposit_npr_label),
                        placeholder = stringResource(Res.string.tenant_security_deposit_placeholder),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    BodyText(
                        text = stringResource(Res.string.utilities_included_label),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BodyText(state.wifiLabel)
                                Switch(checked = state.hasWifi, onCheckedChange = { onAction(EditTenantAction.OnWifiToggled(it)) })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BodyText(state.waterLabel)
                                Switch(checked = state.hasWater, onCheckedChange = { onAction(EditTenantAction.OnWaterToggled(it)) })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BodyText(state.electricityLabel)
                                Switch(checked = state.hasElectricity, onCheckedChange = { onAction(EditTenantAction.OnElectricityToggled(it)) })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BodyText(state.wasteLabel)
                                Switch(checked = state.hasWaste, onCheckedChange = { onAction(EditTenantAction.OnWasteToggled(it)) })
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Section 4: Identification & Documents (Optional) ────────
                val isSec4Complete = !state.documentUrl.isNullOrBlank() || state.pendingDocBytes != null
                val sec4Subtitle = if (isSec4Complete) "${state.documentType} (Attached)" else null

                FormSectionCard(
                    title = stringResource(Res.string.section_documents),
                    subtitle = sec4Subtitle,
                    icon = Icons.Default.Badge,
                    isExpanded = expandedSection == 4,
                    isCompleted = isSec4Complete,
                    onHeaderClick = { expandedSection = if (expandedSection == 4) 0 else 4 }
                ) {
                    Spacer(Modifier.height(8.dp))

                    AppDropdown(
                        options = state.availableDocumentTypes.toPersistentList(),
                        selectedItem = state.documentType,
                        onItemSelected = { onAction(EditTenantAction.OnDocumentTypeSelected(it)) },
                        label = stringResource(Res.string.document_type_label),
                        placeholder = stringResource(Res.string.document_type_placeholder),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(14.dp))

                    AppDocumentPicker(
                        title = stringResource(Res.string.id_proof_upload_label),
                        file = state.documentUrl,
                        previewBytes = state.pendingDocBytes,
                        onClick = { showDocumentSourcePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Total Monthly Rent Summary ──────────────────────────────
                AppCard(
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
        AppDialog(
            icon = Icons.Default.Delete,
            title = stringResource(Res.string.remove_tenant_title),
            body = stringResource(Res.string.remove_tenant_desc),
            confirmText = stringResource(Res.string.remove_action),
            dismissText = stringResource(Res.string.cancel_action),
            onConfirm = { onAction(EditTenantAction.OnDeleteConfirmed) },
            onDismiss = { onAction(EditTenantAction.OnDeleteDismissed) },
            variant = AppDialog.Variant.Destructive
        )
    }
}

// ─── Collapsible Form Section Card ──────────────────────────────────────────

@Composable
private fun FormSectionCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    isExpanded: Boolean,
    isCompleted: Boolean = false,
    onHeaderClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(stiffness = 250f, dampingRatio = 0.72f)
    )

    AppCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted && !isExpanded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = isCompleted && !isExpanded,
                            transitionSpec = {
                                (fadeIn(animationSpec = spring(stiffness = 250f)) +
                                        slideInVertically(animationSpec = spring(stiffness = 250f, dampingRatio = 0.72f)) { -it / 2 })
                                    .togetherWith(
                                        fadeOut(animationSpec = spring(stiffness = 250f)) +
                                                slideOutVertically(animationSpec = spring(stiffness = 250f)) { it / 2 }
                                    )
                            },
                            label = "section_icon_transition"
                        ) { showCheck ->
                            if (showCheck) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        AnimatedVisibility(
                            visible = !isExpanded && !subtitle.isNullOrBlank(),
                            enter = fadeIn(animationSpec = spring(stiffness = 250f)) +
                                    slideInVertically(animationSpec = spring(stiffness = 250f, dampingRatio = 0.72f)) { -it / 2 },
                            exit = fadeOut(animationSpec = spring(stiffness = 250f)) +
                                    slideOutVertically(animationSpec = spring(stiffness = 250f)) { -it / 2 }
                        ) {
                            Text(
                                text = subtitle.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = spring(stiffness = 250f)) +
                        expandVertically(
                            animationSpec = spring(stiffness = 250f, dampingRatio = 0.72f),
                            expandFrom = Alignment.Top
                        ),
                exit = fadeOut(animationSpec = spring(stiffness = 250f)) +
                        shrinkVertically(
                            animationSpec = spring(stiffness = 250f, dampingRatio = 0.78f),
                            shrinkTowards = Alignment.Top
                        )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    content()
                }
            }
        }
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
                        (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.75f)) { height -> height } + fadeIn())
                            .togetherWith(slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { height -> -height } + fadeOut())
                    } else {
                        (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.75f)) { height -> -height } + fadeIn())
                            .togetherWith(slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { height -> height } + fadeOut())
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
            textAlign = TextAlign.Center
        )
    }
}
