package com.gaatho.rent.features.property.presentation.add

import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.*
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
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.features.property.presentation.edit.EditPropertyAction
import com.gaatho.rent.features.property.presentation.edit.EditPropertySideEffect
import com.gaatho.rent.features.property.presentation.edit.EditPropertyViewModel
import com.gaatho.rent.features.property.presentation.edit.PropertyFormContent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun AddPropertyScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditPropertyViewModel = koinViewModel(parameters = { parametersOf("new") })
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
                title = stringResource(Res.string.property_add_new_title),
                onBackClick = { viewModel.onAction(EditPropertyAction.OnBackClicked) },
                containerColor = MaterialTheme.colorScheme.background
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Reuse the same form content for both Add and Edit
        PropertyFormContent(
            state = state,
            onAction = viewModel::onAction,
            isNewProperty = true,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
