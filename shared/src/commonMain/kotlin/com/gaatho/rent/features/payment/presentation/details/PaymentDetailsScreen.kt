package com.gaatho.rent.features.payment.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.core.ui.components.*
import com.gaatho.rent.core.utils.TenantUtils
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
    onNavigateToEdit: (String) -> Unit = {}
) {
    val viewModel: PaymentDetailsViewModel = koinViewModel(parameters = { parametersOf(paymentId) })
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PaymentDetailsSideEffect.NavigateBack -> onNavigateBack()
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
        containerColor = MaterialTheme.colorScheme.background
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
                        modifier = Modifier.fillMaxWidth().padding(horizontal = AppDimensions.ScreenHorizontalPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        PaymentStatusBadge(status = data.payment.status)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ScreenTitle(
                            text = "NPR ${data.payment.amount}",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        BodyText(
                            text = data.payment.date, // You could format this better
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        // Details Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                // Tenant Section
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color(TenantUtils.getAvatarColors(data.tenant?.name ?: "").first)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CardTitle(
                                            text = TenantUtils.getInitials(data.tenant?.name ?: "?")
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        CaptionText(
                                            text = "TENANT",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        CardTitle(
                                            text = data.tenant?.name ?: "Unknown Tenant"
                                        )
                                        if (data.property != null) {
                                            BodySmallText(
                                                text = "${data.tenant?.roomNumber ?: ""} ${data.property.name}",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                
                                DetailRow("Payment Month", "Current Month") // Placeholder or derived
                                
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                
                                DetailRow("Payment Method", data.payment.paymentMethod ?: "Bank Transfer", icon = Icons.Default.AccountBalance)
                                
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                
                                TransactionIdRow(transactionId = "#TXN-${data.payment.id.take(8).uppercase()}")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Breakdown Section
                        CardTitle(
                            text = "Breakdown",
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                BreakdownRow("Base Rent", "NPR ${data.payment.amount}")
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                BreakdownRow("Utilities", "NPR 0")
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    CardTitle(
                                        text = "Total Amount"
                                    )
                                    CardTitle(
                                        text = "NPR ${data.payment.amount}"
                                    )
                                }
                            }
                        }
                        
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

@Composable
private fun PaymentStatusBadge(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "paid" -> AppColors.SuccessContainer to AppColors.Success
        "pending" -> AppColors.WarningContainer to AppColors.Warning
        "overdue" -> AppColors.ErrorContainer to AppColors.Error
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (status.lowercase() == "paid") {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = textColor, modifier = Modifier.size(14.dp))
            }
            LabelText(
                text = status
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyText(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            BodyText(
                text = value
            )
        }
    }
}

@Composable
private fun TransactionIdRow(transactionId: String) {
    val clipboardManager = LocalClipboardManager.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyText(
            text = "Transaction ID",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { 
                clipboardManager.setText(AnnotatedString(transactionId))
            }
        ) {
            BodyText(
                text = transactionId,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.ContentCopy, 
                contentDescription = "Copy", 
                tint = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyText(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BodyText(
            text = value
        )
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
