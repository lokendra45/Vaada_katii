package com.gaatho.rent.features.property.presentation.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.designsystem.components.RentManagerTextField
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: AddPropertyViewModel = koinViewModel()
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AddPropertySideEffect.NavigateBack -> onNavigateBack()
            is AddPropertySideEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
        }
    }

    AddPropertyContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNameChanged = { viewModel.onAction(AddPropertyAction.OnNameChanged(it)) },
        onAddressChanged = { viewModel.onAction(AddPropertyAction.OnAddressChanged(it)) },
        onSaveClicked = { viewModel.onAction(AddPropertyAction.OnSaveClicked) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyContent(
    state: AddPropertyState,
    snackbarHostState: SnackbarHostState,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Property") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 800.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RentManagerTextField(
                    value = state.name,
                    onValueChange = onNameChanged,
                    label = "Property Name",
                    placeholder = "e.g. Peace Villa",
                    isError = state.nameError != null,
                    errorMessage = state.nameError,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving
                )

                RentManagerTextField(
                    value = state.address,
                    onValueChange = onAddressChanged,
                    label = "Property Address",
                    placeholder = "e.g. 123 Main St, Kathmandu",
                    isError = state.addressError != null,
                    errorMessage = state.addressError,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving
                )
                
                // Hardcoded "HOUSE" property type as discussed in the plan.
                RentManagerTextField(
                    value = state.propertyType,
                    onValueChange = { },
                    label = "Property Type",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )

                RentManagerPrimaryButton(
                    text = "Save Property",
                    onClick = onSaveClicked,
                    isLoading = state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
