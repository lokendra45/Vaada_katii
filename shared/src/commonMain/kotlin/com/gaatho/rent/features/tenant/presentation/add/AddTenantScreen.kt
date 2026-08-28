package com.gaatho.rent.features.tenant.presentation.add

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddHome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.ui.components.AppBottomActionBar
import com.gaatho.rent.core.ui.components.AppDateField
import com.gaatho.rent.core.ui.components.AppDatePickerDialog
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.core.ui.components.AppDocumentPicker
import com.gaatho.rent.core.ui.components.AppDropdown
import com.gaatho.rent.core.ui.components.AppImageSourcePicker
import com.gaatho.rent.core.ui.components.AppTextField
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantAction
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantSideEffect
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantState
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantViewModel
import com.gaatho.rent.features.tenant.presentation.edit.PropertyOption
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.add_tenant_btn
import rentmanagerapp.shared.generated.resources.add_tenant_screen_title
import rentmanagerapp.shared.generated.resources.assign_property_label
import rentmanagerapp.shared.generated.resources.assign_property_placeholder
import rentmanagerapp.shared.generated.resources.continue_btn
import rentmanagerapp.shared.generated.resources.id_proof_upload_label
import rentmanagerapp.shared.generated.resources.lease_duration_label
import rentmanagerapp.shared.generated.resources.lease_duration_placeholder
import rentmanagerapp.shared.generated.resources.move_in_date_label
import rentmanagerapp.shared.generated.resources.move_in_date_placeholder
import rentmanagerapp.shared.generated.resources.rent_amount_label
import rentmanagerapp.shared.generated.resources.rent_amount_placeholder
import rentmanagerapp.shared.generated.resources.security_deposit_label
import rentmanagerapp.shared.generated.resources.security_deposit_placeholder
import rentmanagerapp.shared.generated.resources.tenant_email_label
import rentmanagerapp.shared.generated.resources.tenant_email_placeholder
import rentmanagerapp.shared.generated.resources.tenant_full_name_label
import rentmanagerapp.shared.generated.resources.tenant_full_name_placeholder
import rentmanagerapp.shared.generated.resources.tenant_phone_label
import rentmanagerapp.shared.generated.resources.tenant_phone_placeholder
import rentmanagerapp.shared.generated.resources.tenant_saved_body
import rentmanagerapp.shared.generated.resources.tenant_success_title
import rentmanagerapp.shared.generated.resources.unit_number_label
import rentmanagerapp.shared.generated.resources.unit_number_placeholder

// ─── Stateful entry point ─────────────────────────────────────────────────────

@Composable
fun AddTenantScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditTenantViewModel = koinViewModel(parameters = { parametersOf("new") })
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is EditTenantSideEffect.NavigateBack -> onNavigateBack()
            is EditTenantSideEffect.ShowSnackbar ->
                snackbarHostState.showSnackbar(effect.message)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.add_tenant_screen_title),
                onBackClick = { viewModel.onAction(EditTenantAction.OnBackClicked) },
                containerColor = MaterialTheme.colorScheme.background,
                titleStyle = MaterialTheme.typography.headlineMedium
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.propertyOptions.isNotEmpty()) {
                AppBottomActionBar {
                    RentManagerPrimaryButton(
                        text = stringResource(Res.string.add_tenant_btn),
                        onClick = { viewModel.onAction(EditTenantAction.OnSaveClicked) },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = state.isSaving
                    )
                }
            } else {
                AppBottomActionBar {
                    RentManagerPrimaryButton(
                        text = stringResource(Res.string.add_tenant_btn),
                        onClick = { /* disabled */ },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = false,
                        enabled = false
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        com.gaatho.rent.features.tenant.presentation.edit.EditTenantContent(
            state = state,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
