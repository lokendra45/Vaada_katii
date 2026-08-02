package com.gaatho.rent.features.tenant.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

import com.gaatho.rent.core.designsystem.AppColors

// ─── Entry point ─────────────────────────────────────────────────────────────

@Composable
fun TenantDetailsScreen(
    tenantId: String,
    onNavigateBack: () -> Unit,
    viewModel: TenantDetailsViewModel = koinInject(parameters = { parametersOf(tenantId) })
) {
    val state by viewModel.container.stateFlow.collectAsState()

    LaunchedEffect(viewModel.container.sideEffectFlow) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is TenantDetailsEffect.NavigateBack          -> onNavigateBack()
                is TenantDetailsEffect.OpenEmailApp          -> {}
                is TenantDetailsEffect.OpenPhoneApp          -> {}
                is TenantDetailsEffect.NavigateToTransactions -> {}
                is TenantDetailsEffect.ShowToast             -> {}
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
        topBar = { TenantTopBar(onAction) },
        // Use surface instead of background to avoid pure black in dark mode
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddings ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // ── Profile ───────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            when (val s = state.profileState) {
                is UiState.Success -> ProfileHeaderSection(s.data, onAction)
                is UiState.Loading -> Box(
                    Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                else -> {}
            }

            // ── Lease Details ─────────────────────────────────────────────────
            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Spacer(Modifier.height(32.dp))
            when (val s = state.leaseState) {
                is UiState.Success -> LeaseDetailsSection(s.data)
                else -> {}
            }

            // ── Transactions ──────────────────────────────────────────────────
            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Spacer(Modifier.height(32.dp))
            when (val s = state.transactionsState) {
                is UiState.Success -> TransactionsSection(s.data, onAction)
                else -> {}
            }

            Spacer(Modifier.height(64.dp))
        }
    }
}

// ─── Top App Bar ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TenantTopBar(onAction: (TenantDetailsAction) -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Rent Manager",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(onClick = { onAction(TenantDetailsAction.OnBackClicked) }) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    )
}

// ─── Profile Header ───────────────────────────────────────────────────────────

@Composable
private fun ProfileHeaderSection(
    profile: TenantProfileDisplayModel,
    onAction: (TenantDetailsAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = profile.name,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = profile.address,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline)
            )
            if (profile.isVerified) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        Icons.Filled.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Verified",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Primary quick-actions ─────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TenantQuickAction(
                icon = Icons.Outlined.AddCard,
                label = "Payment",
                modifier = Modifier.weight(1f),
                onClick = { onAction(TenantDetailsAction.OnPaymentClicked) }
            )
            TenantQuickAction(
                icon = Icons.Outlined.Mail,
                label = "Email",
                modifier = Modifier.weight(1f),
                onClick = { onAction(TenantDetailsAction.OnEmailClicked) }
            )
            TenantQuickAction(
                icon = Icons.Outlined.Call,
                label = "Call",
                modifier = Modifier.weight(1f),
                onClick = { onAction(TenantDetailsAction.OnCallClicked) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Secondary actions ───────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
        ) {
            TenantSecondaryAction(
                icon = Icons.AutoMirrored.Outlined.Message,
                label = "Message",
                onClick = { onAction(TenantDetailsAction.OnMessageClicked) }
            )
            TenantSecondaryAction(
                icon = Icons.Outlined.Build,
                label = "Maintenance",
                onClick = { onAction(TenantDetailsAction.OnMaintenanceClicked) }
            )
        }
    }
}

// ─── Quick Action Button ─────────────────────────────

@Composable
private fun TenantQuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant
        ),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─── Secondary text-only action button ────────────────────────────────────────

@Composable
private fun TenantSecondaryAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Lease Details ────────────────────────────────────────────────────────────

@Composable
private fun LeaseDetailsSection(lease: TenantLeaseDisplayModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Lease Details",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    LeaseCell(
                        label = "Monthly Rent",
                        value = lease.monthlyRent,
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    LeaseCell(
                        label = "Status",
                        value = lease.status,
                        valueColor = if (lease.isActive) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    LeaseCell(
                        label = "Start Date",
                        value = lease.startDate,
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    LeaseCell(
                        label = "End Date",
                        value = lease.endDate,
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Lease Term".uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = lease.leaseTerm,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (lease.isRenewable) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "Renewable",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaseCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = valueColor
        )
    }
}

// ─── Transactions ─────────────────────────────────────────────────────────────

@Composable
private fun TransactionsSection(
    transactions: ImmutableList<TenantTransactionDisplayModel>,
    onAction: (TenantDetailsAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable { onAction(TenantDetailsAction.OnViewAllTransactionsClicked) }
                    .padding(4.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            transactions.forEach { tx ->
                TransactionRow(
                    tx = tx,
                    onClick = { onAction(TenantDetailsAction.OnTransactionClicked(tx.id)) }
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: TenantTransactionDisplayModel, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tx.type.contains("Deposit", ignoreCase = true))
                        Icons.Outlined.AccountBalanceWallet
                    else
                        Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.type,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tx.date,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = tx.amount,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (tx.isPaid) AppColors.Success else MaterialTheme.colorScheme.error)
                    )
                    Text(
                        text = tx.status,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private fun previewState() = TenantDetailsState(
    tenantId = "1",
    profileState = UiState.Success(
        TenantProfileDisplayModel(
            id = "1", name = "Suman Shrestha",
            address = "Bakhundole, Lalitpur", isVerified = true
        )
    ),
    leaseState = UiState.Success(
        TenantLeaseDisplayModel(
            monthlyRent = "रू 45,000", status = "Active", isActive = true,
            startDate = "Sept 1, 2023", endDate = "Aug 31, 2024",
            leaseTerm = "12 Months", isRenewable = true
        )
    ),
    transactionsState = UiState.Success(
        persistentListOf(
            TenantTransactionDisplayModel("t1", "Rent Payment",    "Nov 1, 2023",  "रू 45,000", "Paid", true),
            TenantTransactionDisplayModel("t2", "Rent Payment",    "Oct 1, 2023",  "रू 45,000", "Paid", true),
            TenantTransactionDisplayModel("t3", "Security Deposit","Aug 25, 2023", "रू 90,000", "Paid", true)
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
