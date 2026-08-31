package com.gaatho.rent.features.payment.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.core.ui.components.*
import com.gaatho.rent.features.payment.presentation.receipt.ReceiptCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.cancel_action
import rentmanagerapp.shared.generated.resources.delete_action
import rentmanagerapp.shared.generated.resources.delete_payment_desc
import rentmanagerapp.shared.generated.resources.delete_payment_title
import rentmanagerapp.shared.generated.resources.download_receipt_action
import rentmanagerapp.shared.generated.resources.retry
import rentmanagerapp.shared.generated.resources.share_details_action

@Composable
fun PaymentDetailsScreen(
    paymentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPaymentList: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {}
) {
    val viewModel: PaymentDetailsViewModel = koinViewModel(parameters = { parametersOf(paymentId) })
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PaymentDetailsSideEffect.NavigateBack -> onNavigateBack()
            is PaymentDetailsSideEffect.NavigateToPaymentList -> onNavigateToPaymentList()
            is PaymentDetailsSideEffect.ShowError -> {}
            is PaymentDetailsSideEffect.ShowMessage -> {}
        }
    }

    PaymentDetailsContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToEdit = { onNavigateToEdit(paymentId) },
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDetailsContent(
    state: PaymentDetailsState,
    onAction: (PaymentDetailsAction) -> Unit,
    onNavigateToEdit: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Payment Details",
                onBackClick = { onAction(PaymentDetailsAction.OnBackClicked) },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onAction(PaymentDetailsAction.OnDeleteClicked) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (state.paymentState is UiState.Success) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimensions.ScreenHorizontalPadding)
                            .padding(bottom = 16.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onAction(PaymentDetailsAction.OnDownloadReceipt) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.download_receipt_action), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }

                        Button(
                            onClick = { onAction(PaymentDetailsAction.OnShareDetails) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.share_details_action), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            when (val result = state.paymentState) {
                is UiState.Idle -> {}
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = result.message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = { onAction(PaymentDetailsAction.OnRetry) }) {
                                Text(stringResource(Res.string.retry))
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val data = result.data
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ReceiptCard(
                            amount = data.payment.amount.toString(),
                            tenantName = data.tenant?.name ?: "Unknown Tenant",
                            propertyName = data.property?.name ?: "Unknown Property",
                            date = data.payment.date,
                            paymentMethod = data.payment.paymentMethod ?: "Bank Transfer",
                            transactionId = "TXN-${data.payment.id.take(8).uppercase()}"
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        if (state.showDeleteConfirm) {
            com.gaatho.rent.core.ui.components.AppDialog(
                icon = Icons.Default.Delete,
                title = stringResource(Res.string.delete_payment_title),
                body = stringResource(Res.string.delete_payment_desc),
                confirmText = stringResource(Res.string.delete_action),
                dismissText = stringResource(Res.string.cancel_action),
                onConfirm = { onAction(PaymentDetailsAction.OnDeleteConfirmed) },
                onDismiss = { onAction(PaymentDetailsAction.OnDeleteDismissed) },
                variant = com.gaatho.rent.core.ui.components.AppDialog.Variant.Destructive
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PaymentDetailsScreenPreview() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        PaymentDetailsContent(
            state = PaymentDetailsState(
                paymentState = UiState.Success(
                    PaymentDetailsData(
                        payment = com.gaatho.rent.features.payment.domain.model.Payment(
                            id = "txn-12345678",
                            ownerId = "owner-1",
                            tenantId = "tenant-1",
                            propertyId = "prop-1",
                            amount = 15000,
                            date = "July 24, 2024 at 10:45 AM",
                            status = "Paid",
                            paymentMethod = "Bank Transfer",
                            notes = null,
                            createdAt = "2024-07-24T10:45:00Z",
                            updatedAt = "2024-07-24T10:45:00Z"
                        ),
                        tenant = com.gaatho.rent.features.tenant.domain.model.Tenant(
                            id = "tenant-1",
                            ownerId = "owner-1",
                            name = "Anita Basnet",
                            roomNumber = "Unit 4A",
                            createdAt = "2024-07-24T10:45:00Z",
                            updatedAt = "2024-07-24T10:45:00Z"
                        ),
                        property = com.gaatho.rent.features.property.domain.model.Property(
                            id = "prop-1",
                            ownerId = "owner-1",
                            name = "Sunrise Residency",
                            address = "Kathmandu",
                            propertyType = "Apartment",
                            createdAt = "2024-07-24T10:45:00Z",
                            updatedAt = "2024-07-24T10:45:00Z"
                        )
                    )
                )
            ),
            onAction = {},
            onNavigateToEdit = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PaymentDetailsScreenDarkPreview() {
    com.gaatho.rent.core.designsystem.RentManagerTheme(darkTheme = true) {
        PaymentDetailsContent(
            state = PaymentDetailsState(
                paymentState = UiState.Success(
                    PaymentDetailsData(
                        payment = com.gaatho.rent.features.payment.domain.model.Payment(
                            id = "txn-12345678",
                            ownerId = "owner-1",
                            tenantId = "tenant-1",
                            propertyId = "prop-1",
                            amount = 15000L,
                            date = "July 24, 2024 at 10:45 AM",
                            status = "Paid",
                            paymentMethod = "Bank Transfer",
                            notes = null,
                            createdAt = "2024-07-24T10:45:00Z",
                            updatedAt = "2024-07-24T10:45:00Z"
                        ),
                        tenant = com.gaatho.rent.features.tenant.domain.model.Tenant(
                            id = "tenant-1",
                            ownerId = "owner-1",
                            name = "Anita Basnet",
                            roomNumber = "Unit 4A",
                            createdAt = "2024-07-24T10:45:00Z",
                            updatedAt = "2024-07-24T10:45:00Z"
                        ),
                        property = com.gaatho.rent.features.property.domain.model.Property(
                            id = "prop-1",
                            ownerId = "owner-1",
                            name = "Sunrise Residency",
                            address = "Kathmandu",
                            propertyType = "Apartment",
                            createdAt = "2024-07-24T10:45:00Z",
                            updatedAt = "2024-07-24T10:45:00Z"
                        )
                    )
                )
            ),
            onAction = {},
            onNavigateToEdit = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}
