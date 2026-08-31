package com.gaatho.rent.features.tenant.presentation.details

import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.*
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppActionPill
import com.gaatho.rent.core.ui.components.*
import com.gaatho.rent.core.ui.components.AppStatusBadge
import com.gaatho.rent.core.utils.TenantUtils
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// ─── Entry point ─────────────────────────────────────────────────────────────

@Composable
fun TenantDetailsScreen(
    tenantId: String,
    onNavigateBack: () -> Unit,
    onNavigateToTenantList: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    viewModel: TenantDetailsViewModel = koinViewModel(parameters = { parametersOf(tenantId) })
) {
    LaunchedEffect(viewModel.container.sideEffectFlow) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is TenantDetailsEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    TenantDetailsContent(
        tenantId = tenantId,
        onAction = viewModel::onAction,
        onNavigateToEdit = onNavigateToEdit,
        onNavigateToTenantList = onNavigateToTenantList
    )
}

// Removed LaunchEffect here since it's merged above

// ─── Content ─────────────────────────────────────────────────────────────────

@Composable
private fun TenantDetailsContent(
    tenantId: String,
    onAction: (TenantDetailsAction) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToTenantList: () -> Unit
) {
    Scaffold(
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = stringResource(Res.string.tenant_profile_title),
                onBackClick = { onAction(TenantDetailsAction.OnBackClicked) },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(tenantId) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { paddings ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            com.gaatho.rent.core.ui.components.AppSearchBar(
                query = "",
                onQueryChange = {},
                placeholderText = "Search tenants...",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            
            ProfileSection(tenantId = tenantId, onNavigateToEdit = onNavigateToEdit, onNavigateToTenantList = onNavigateToTenantList)

            Spacer(Modifier.height(24.dp))
            
            LeaseSection(tenantId = tenantId)

            Spacer(Modifier.height(24.dp))
            
            TransactionsSection(tenantId = tenantId)

            Spacer(Modifier.height(64.dp))
        }
    }
}

@Composable
private fun ProfileSection(
    tenantId: String,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToTenantList: () -> Unit
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner()
    val viewModel = koinViewModel<TenantProfileViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
        parameters = { parametersOf(tenantId) }
    )
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    LaunchedEffect(viewModel.container.sideEffectFlow) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is TenantProfileEffect.NavigateToEdit -> onNavigateToEdit(effect.tenantId)
                is TenantProfileEffect.NavigateBack -> {} // Handle back if deleted
                is TenantProfileEffect.NavigateToTenantList -> onNavigateToTenantList()
                is TenantProfileEffect.OpenEmailApp -> {
                    try { uriHandler.openUri("mailto:${effect.email}") } catch (e: Exception) {}
                }
                is TenantProfileEffect.OpenPhoneApp -> {
                    try { uriHandler.openUri("tel:${effect.phone}") } catch (e: Exception) {}
                }
                is TenantProfileEffect.OpenSmsApp -> {
                    try { uriHandler.openUri("sms:${effect.phone}") } catch (e: Exception) {}
                }
                is TenantProfileEffect.ShowToast -> {}
                is TenantProfileEffect.ShowError -> {}
            }
        }
    }

    when (val s = state.profileState) {
        is UiState.Success -> ProfileCard(s.data, viewModel::onAction)
        is UiState.Loading -> TenantDetailsSkeleton()
        else -> {}
    }

    if (state.showDeleteConfirm) {
        com.gaatho.rent.core.ui.components.AppDialog(
            variant = com.gaatho.rent.core.ui.components.AppDialog.Variant.Destructive,
            layout = com.gaatho.rent.core.ui.components.AppDialog.Layout.Center,
            icon = Icons.Default.Delete,
            title = stringResource(Res.string.tenant_delete_dialog_title),
            body = stringResource(Res.string.tenant_delete_dialog_body),
            confirmText = stringResource(Res.string.delete_action),
            dismissText = stringResource(Res.string.cancel_action),
            onConfirm = { viewModel.onAction(TenantProfileAction.OnDeleteConfirmed) },
            onDismiss = { viewModel.onAction(TenantProfileAction.OnDeleteDismissed) }
        )
    }
}

@Composable
private fun LeaseSection(
    tenantId: String
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner()
    val viewModel = koinViewModel<TenantLeaseViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
        parameters = { parametersOf(tenantId) }
    )
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    
    LaunchedEffect(viewModel.container.sideEffectFlow) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is TenantLeaseEffect.ShowToast -> {}
            }
        }
    }

    when (val s = state.leaseState) {
        is UiState.Success -> RentDetailsSection(s.data)
        else -> {}
    }
}

@Composable
private fun TransactionsSection(
    tenantId: String
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner()
    val viewModel = koinViewModel<TenantTransactionsViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
        parameters = { parametersOf(tenantId) }
    )
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    
    LaunchedEffect(viewModel.container.sideEffectFlow) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is TenantTransactionsEffect.NavigateToTransactions -> {}
                is TenantTransactionsEffect.ShowToast -> {}
            }
        }
    }

    when (val s = state.transactionsState) {
        is UiState.Success -> PaymentHistorySection(s.data, viewModel::onAction)
        else -> {}
    }
}

// Delete confirm handled inside ProfileSection now

// ─── Profile Card ─────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(
    profile: TenantProfileDisplayModel,
    onAction: (TenantProfileAction) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            com.gaatho.rent.core.ui.components.AppAsyncImage(
                model = profile.avatarUrl,
                contentDescription = "Profile Photo",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                placeholderType = com.gaatho.rent.core.ui.components.PlaceholderType.AVATAR
            )

            Spacer(Modifier.height(12.dp))

            CardTitle(
                text = profile.name,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            BodyText(
                text = profile.phone?.takeIf { it.isNotBlank() } ?: "No phone added",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(2.dp))

            BodySmallText(
                text = profile.movedInDate?.let { "Moved in: $it" } ?: "Moved in: —",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (profile.roomNumber != null) {
                Spacer(Modifier.height(4.dp))
                com.gaatho.rent.core.ui.components.AppStatusBadge(
                    label = profile.roomNumber,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppActionPill(
                    icon = Icons.Outlined.Call,
                        label = stringResource(Res.string.tenant_call_action), // Should ideally be string resource, but keeping it simple for now
                    onClick = { onAction(TenantProfileAction.OnCallClicked) },
                    modifier = Modifier.weight(1f)
                )
                AppActionPill(
                    icon = Icons.Outlined.Notifications,
                        label = stringResource(Res.string.tenant_remind_action), // Should ideally be string resource
                    onClick = { onAction(TenantProfileAction.OnMessageClicked) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (profile.documentUrl != null) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = AppColors.CardBorder)
                Spacer(Modifier.height(16.dp))
                
                var showFullScreenDoc by remember { mutableStateOf(false) }
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    CardTitle(
                        text = stringResource(Res.string.tenant_id_proof_prefix) + (profile.documentType ?: stringResource(Res.string.tenant_document_fallback)),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    Surface(
                        onClick = { showFullScreenDoc = true },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        com.gaatho.rent.core.ui.components.AppAsyncImage(
                            model = profile.documentUrl,
                            contentDescription = "ID Proof",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                if (showFullScreenDoc) {
                    FullScreenImageViewer(
                        imageUrl = profile.documentUrl,
                        onDismiss = { showFullScreenDoc = false }
                    )
                }
            }
        }
    }
}



// ─── Rent Details ─────────────────────────────────────────────────────────────

@Composable
private fun RentDetailsSection(lease: TenantLeaseDisplayModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CardTitle(
            text = stringResource(Res.string.tenant_rent_details_label),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                RentInfoRow(
                    label = stringResource(Res.string.tenant_monthly_rent_label),
                    value = lease.monthlyRent,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = AppColors.CardBorder)
                RentInfoRow(
                    label = stringResource(Res.string.tenant_lease_duration_label),
                    value = lease.leaseTerm,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                if (lease.roomNumber != null) {
                    HorizontalDivider(color = AppColors.CardBorder)
                    RentInfoRow(
                        label = stringResource(Res.string.tenant_unit_room_label),
                        value = lease.roomNumber,
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(color = AppColors.CardBorder)
                RentInfoRow(
                    label = stringResource(Res.string.tenant_security_deposit_label),
                    value = lease.securityDeposit ?: "—",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = AppColors.CardBorder)
                RentInfoRow(
                    label = stringResource(Res.string.tenant_payment_due_date_label),
                    value = lease.paymentDueDate ?: "—",
                    valueColor = AppColors.Error
                )
            }
        }
    }
}

@Composable
private fun RentInfoRow(
    label: String,
    value: String,
    valueColor: Color
) {
    InfoRow(
        label = label,
        value = value,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        valueColor = valueColor
    )
}

// ─── Payment History ──────────────────────────────────────────────────────────

@Composable
private fun PaymentHistorySection(
    transactions: ImmutableList<TenantTransactionDisplayModel>,
    onAction: (TenantTransactionsAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CardTitle(
            text = stringResource(Res.string.tenant_payment_history_label),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            transactions.forEach { tx ->
                HistoryRow(
                    tx = tx,
                    onClick = { onAction(TenantTransactionsAction.OnTransactionClicked(tx.id)) }
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(tx: TenantTransactionDisplayModel, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                CardTitle(
                    text = tx.date,
                    color = MaterialTheme.colorScheme.onSurface
                )
                BodySmallText(
                    text = buildString {
                        append(tx.amount)
                        tx.method?.takeIf { it.isNotBlank() }?.let { append(" • $it") }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val isDarkTheme = com.gaatho.rent.core.environment.LocalAppTheme.current
            val isPaid = tx.isPaid
            
            AppStatusBadge(
                label = tx.status,
                containerColor = if (isDarkTheme) {
                    if (isPaid) MaterialTheme.colorScheme.primaryContainer else com.gaatho.rent.core.designsystem.AppColors.WarningContainer
                } else {
                    if (isPaid) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else com.gaatho.rent.core.designsystem.AppColors.Warning.copy(alpha = 0.15f)
                },
                contentColor = if (isDarkTheme) {
                    if (isPaid) MaterialTheme.colorScheme.onPrimaryContainer else com.gaatho.rent.core.designsystem.AppColors.OnWarning
                } else {
                    if (isPaid) MaterialTheme.colorScheme.primary else com.gaatho.rent.core.designsystem.AppColors.Warning
                },
                horizontalPadding = 12.dp,
                verticalPadding = 5.dp
            )
        }
    }
}



// ─── Previews ─────────────────────────────────────────────────────────────────

// Previews mock the unified state slightly differently now, so we remove the previewState() 
// since the sub-components manage their own states. We rely on the composable previews 
// displaying the empty states.

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewLight() {
    RentManagerTheme(darkTheme = false) {
        TenantDetailsContent(tenantId = "1", onAction = {}, onNavigateToEdit = {}, onNavigateToTenantList = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewDark() {
    RentManagerTheme(darkTheme = true) {
        TenantDetailsContent(tenantId = "1", onAction = {}, onNavigateToEdit = {}, onNavigateToTenantList = {})
    }
}

@Composable
private fun TenantDetailsSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        com.gaatho.rent.core.ui.components.AppShimmerBox(
            modifier = Modifier.size(100.dp).clip(CircleShape)
        )
        Spacer(Modifier.height(16.dp))
        com.gaatho.rent.core.ui.components.AppShimmerBox(
            modifier = Modifier.width(150.dp).height(24.dp).clip(RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.height(8.dp))
        com.gaatho.rent.core.ui.components.AppShimmerBox(
            modifier = Modifier.width(100.dp).height(16.dp).clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.9f))
                .clickable { onDismiss() }
        ) {
            com.gaatho.rent.core.ui.components.AppAsyncImage(
                model = imageUrl,
                contentDescription = "Full Screen Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}