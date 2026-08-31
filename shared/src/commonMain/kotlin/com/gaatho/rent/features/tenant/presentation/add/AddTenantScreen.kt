package com.gaatho.rent.features.tenant.presentation.add

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.ui.components.AppBottomActionBar
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantAction
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantSideEffect
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.add_tenant_btn
import rentmanagerapp.shared.generated.resources.add_tenant_screen_title

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
            is EditTenantSideEffect.NavigateToTenantList -> onNavigateBack()
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
                        isLoading = state.isSaving,
                        enabled = state.isFormValid && !state.isSaving
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
