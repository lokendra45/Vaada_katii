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
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

// ─── Entry point ─────────────────────────────────────────────────────────────

@Composable
fun TenantDetailsScreen(
    tenantId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    viewModel: TenantDetailsViewModel = koinInject(parameters = { parametersOf(tenantId) })
) {
    val state by viewModel.container.stateFlow.collectAsState()

    LaunchedEffect(viewModel.container.sideEffectFlow) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is TenantDetailsEffect.NavigateBack          -> onNavigateBack()
                is TenantDetailsEffect.NavigateToEdit        -> onNavigateToEdit(effect.tenantId)
                is TenantDetailsEffect.OpenEmailApp          -> {}
                is TenantDetailsEffect.OpenPhoneApp          -> {}
                is TenantDetailsEffect.NavigateToTransactions -> {}
                is TenantDetailsEffect.ShowToast             -> {}
                is TenantDetailsEffect.ShowError             -> {}
            }
        }
    }

    TenantDetailsContent(state = state, onAction = viewModel::onAction)
}

// ─── Content ─────────────────────────────────────────────────────────────────

@Composable
private fun TenantDetailsContent(
    state: TenantDetailsState,
    onAction: (TenantDetailsAction) -> Unit
) {
    Scaffold(
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = "Tenant Profile",
                onBackClick = { onAction(TenantDetailsAction.OnBackClicked) },
                actions = {
                    IconButton(onClick = { onAction(TenantDetailsAction.OnEditClicked) }) {
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
            when (val s = state.profileState) {
                is UiState.Success -> ProfileCard(s.data, onAction)
                is UiState.Loading -> { TenantDetailsSkeleton() }
                else -> {}
            }

            Spacer(Modifier.height(24.dp))
            when (val s = state.leaseState) {
                is UiState.Success -> RentDetailsSection(s.data)
                else -> {}
            }

            Spacer(Modifier.height(24.dp))
            when (val s = state.transactionsState) {
                is UiState.Success -> PaymentHistorySection(s.data, onAction)
                else -> {}
            }

            Spacer(Modifier.height(64.dp))
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
                onConfirm = { onAction(TenantDetailsAction.OnDeleteConfirmed) },
                onDismiss = { onAction(TenantDetailsAction.OnDeleteDismissed) }
            )
        }
    }
}

// ─── Profile Card ─────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(
    profile: TenantProfileDisplayModel,
    onAction: (TenantDetailsAction) -> Unit
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
                ActionPill(
                    icon = Icons.Outlined.Call,
                    label = "Call",
                    onClick = { onAction(TenantDetailsAction.OnCallClicked) },
                    modifier = Modifier.weight(1f)
                )
                ActionPill(
                    icon = Icons.Outlined.Notifications,
                    label = "Remind",
                    onClick = { onAction(TenantDetailsAction.OnPaymentClicked) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = AppColors.EmeraldAccentLight,
        modifier = modifier.wrapContentSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.EmeraldAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.EmeraldAccent
                )
            )
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
    onAction: (TenantDetailsAction) -> Unit
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
                    onClick = { onAction(TenantDetailsAction.OnTransactionClicked(tx.id)) }
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

            StatusPill(status = tx.status, isPaid = tx.isPaid)
        }
    }
}

@Composable
private fun StatusPill(status: String, isPaid: Boolean) {
    val (bg, text) = if (isPaid) {
        AppColors.EmeraldAccentLight to AppColors.EmeraldAccent
    } else {
        Color(0xFFFFF7E8) to Color(0xFFF59E0B)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = text
            )
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private fun previewState() = TenantDetailsState(
    tenantId = "1",
    profileState = UiState.Success(
        TenantProfileDisplayModel(
            id = "1", name = "Suman Maharjan",
            address = "Sundar Niwas", isVerified = true,
            phone = "+977 98510-23456",
            movedInDate = "12 July 2023"
        )
    ),
    leaseState = UiState.Success(
        TenantLeaseDisplayModel(
            monthlyRent = "NPR 25,000", status = "Active", isActive = true,
            startDate = "July 12, 2023", endDate = "Ongoing",
            securityDeposit = "NPR 50,000",
            paymentDueDate = "5th of every month"
        )
    ),
    transactionsState = UiState.Success(
        persistentListOf(
            TenantTransactionDisplayModel("t1", "Rent Payment", "Ashwin 2080", "NPR 25,000", "Paid", true, "eSewa"),
            TenantTransactionDisplayModel("t2", "Rent Payment", "Bhadra 2080", "NPR 25,000", "Paid", true, "Cash"),
            TenantTransactionDisplayModel("t3", "Rent Payment", "Shrawan 2080", "NPR 25,000", "Pending", false, "Bank")
        )
    )
)

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewLight() {
    RentManagerTheme(darkTheme = false) {
        TenantDetailsContent(state = previewState(), onAction = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewDark() {
    RentManagerTheme(darkTheme = true) {
        TenantDetailsContent(state = previewState(), onAction = {})
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