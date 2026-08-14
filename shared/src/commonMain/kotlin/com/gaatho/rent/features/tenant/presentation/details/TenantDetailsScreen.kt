package com.gaatho.rent.features.tenant.presentation.details

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppActionPill
import com.gaatho.rent.core.ui.components.AppStatusBadge
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// ─── Entry point ─────────────────────────────────────────────────────────────

@Composable
fun TenantDetailsScreen(
    tenantId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    viewModel: TenantDetailsViewModel = koinInject(parameters = { parametersOf(tenantId) })
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
        onNavigateToEdit = onNavigateToEdit
    )
}

// Removed LaunchEffect here since it's merged above

// ─── Content ─────────────────────────────────────────────────────────────────

@Composable
private fun TenantDetailsContent(
    tenantId: String,
    onAction: (TenantDetailsAction) -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    Scaffold(
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = "Tenant Profile",
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
            
            ProfileSection(tenantId = tenantId, onNavigateToEdit = onNavigateToEdit)

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
    onNavigateToEdit: (String) -> Unit
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner()
    val viewModel = koinViewModel<TenantProfileViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
        parameters = { parametersOf(tenantId) }
    )
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.container.sideEffectFlow) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is TenantProfileEffect.NavigateToEdit -> onNavigateToEdit(effect.tenantId)
                is TenantProfileEffect.NavigateBack -> {} // Handle back if deleted
                is TenantProfileEffect.OpenEmailApp -> {}
                is TenantProfileEffect.OpenPhoneApp -> {}
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
            title = "Delete Tenant",
            body = "Are you sure you want to delete this tenant? This action cannot be undone.",
            confirmText = "Delete",
            dismissText = "Cancel",
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
            val initials = profile.name.split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AppColors.EmeraldAccentLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.EmeraldAccent
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = profile.phone?.takeIf { it.isNotBlank() } ?: "No phone added",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = profile.movedInDate?.let { "Moved in: $it" } ?: "Moved in: —",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppActionPill(
                    icon = Icons.Outlined.Call,
                    label = "Call", // Should ideally be string resource, but keeping it simple for now
                    onClick = { onAction(TenantProfileAction.OnCallClicked) },
                    modifier = Modifier.weight(1f)
                )
                AppActionPill(
                    icon = Icons.Outlined.Notifications,
                    label = "Remind", // Should ideally be string resource
                    onClick = { onAction(TenantProfileAction.OnMessageClicked) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}



// ─── Rent Details ─────────────────────────────────────────────────────────────

@Composable
private fun RentDetailsSection(lease: TenantLeaseDisplayModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Rent Details",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                    label = "Monthly Rent",
                    value = lease.monthlyRent,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = AppColors.CardBorder)
                RentInfoRow(
                    label = "Security Deposit",
                    value = lease.securityDeposit ?: "—",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = AppColors.CardBorder)
                RentInfoRow(
                    label = "Payment Due Date",
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            fontSize = 13.sp,
            color = valueColor
        )
    }
}

// ─── Payment History ──────────────────────────────────────────────────────────

@Composable
private fun PaymentHistorySection(
    transactions: ImmutableList<TenantTransactionDisplayModel>,
    onAction: (TenantTransactionsAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Payment History",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                Text(
                    text = tx.date,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = buildString {
                        append(tx.amount)
                        tx.method?.takeIf { it.isNotBlank() }?.let { append(" • $it") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
            }

            AppStatusBadge(
                label = tx.status,
                containerColor = if (tx.isPaid) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                contentColor = if (tx.isPaid) MaterialTheme.colorScheme.primary else Color(0xFFF59E0B),
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
        TenantDetailsContent(tenantId = "1", onAction = {}, onNavigateToEdit = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewDark() {
    RentManagerTheme(darkTheme = true) {
        TenantDetailsContent(tenantId = "1", onAction = {}, onNavigateToEdit = {})
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