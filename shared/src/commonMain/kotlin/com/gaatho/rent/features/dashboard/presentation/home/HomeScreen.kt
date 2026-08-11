package com.gaatho.rent.features.dashboard.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.Spacing
import com.gaatho.rent.core.designsystem.softShadow
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun HomeScreen(
    onNavigateToAddTenant: () -> Unit = {},
    onNavigateToAddProperty: () -> Unit = {},
    onNavigateToAddPayment: () -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
    onNavigateToTenantDetails: (String) -> Unit = {}
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is HomeSideEffect.NavigateToAddTenant -> onNavigateToAddTenant()
            is HomeSideEffect.NavigateToAddProperty -> onNavigateToAddProperty()
            is HomeSideEffect.NavigateToAddPayment -> onNavigateToAddPayment()
            is HomeSideEffect.NavigateToPayments -> onNavigateToPayments()
            is HomeSideEffect.NavigateToTenantDetails -> onNavigateToTenantDetails(effect.tenantId)
            is HomeSideEffect.NavigateToExpenses -> { /* Handle later */ }
        }
    }

    HomeContent(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun HomeContent(
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest, // Pure white background
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = "Vaada",
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = state.userName.firstOrNull()?.toString()?.uppercase() ?: "U"
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Spacing.ScreenPadding,
                end = Spacing.ScreenPadding,
                top = 8.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.SectionGap)
        ) {
            item {
                // Local search state — never routed through Orbit to avoid recompositions
                var searchQuery by remember { mutableStateOf("") }
                com.gaatho.rent.core.ui.components.AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholderText = "Search tenants, properties",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.isLoading) {
                item { HomeSkeletonLoadingState() }
            } else if (state.propertiesCount == 0) {
                // Empty State Layout
                item { EmptyStateSection(onAction) }
                item { QuickActionsSection(onAction, isDisabled = true) }
                item { RecentPaymentsEmpty() }
            } else {
                // Filled State Layout
                item { QuickActionsSection(onAction, isDisabled = false) }
                item { DashboardCard(state) }
                if (state.overdueTenantsCount > 0) {
                    item { OverdueAlertBanner(state.overdueTenantsCount) }
                }
                item { RecentPaymentsSection(state, onAction) }
            }
        }
    }
}

// We removed SearchBarSection because we are using AppSearchBar

@Composable
private fun EmptyStateSection(onAction: (HomeAction) -> Unit) {
    com.gaatho.rent.core.ui.components.EmptyStateCard(
        icon = Icons.Outlined.Business,
        title = "No properties found",
        description = "Start by adding a property to track rent, tenants, and payments.",
        buttonText = "Add property",
        onButtonClick = { onAction(HomeAction.OnAddPropertyClicked) }
    )
}

@Composable
private fun QuickActionsSection(onAction: (HomeAction) -> Unit, isDisabled: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (isDisabled) {
            Text(
                text = "Quick actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val disabledContainer = MaterialTheme.colorScheme.surfaceContainerHighest
            val disabledIcon = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            val disabledText = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            
            // Finzo aesthetic: Muted, clean circular actions instead of loud semantic colors
            val defaultContainer = MaterialTheme.colorScheme.surfaceContainer // Soft slate
            val defaultIcon = MaterialTheme.colorScheme.onSurface // Deep slate
            
            QuickActionItem(
                label = "Property",
                icon = Icons.Outlined.Business,
                containerColor = if (isDisabled) disabledContainer else defaultContainer,
                iconColor = if (isDisabled) disabledIcon else defaultIcon,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = { if (!isDisabled) onAction(HomeAction.OnAddPropertyClicked) }
            )
            QuickActionItem(
                label = "Record pay",
                icon = Icons.Outlined.AccountBalanceWallet,
                containerColor = if (isDisabled) disabledContainer else defaultContainer,
                iconColor = if (isDisabled) disabledIcon else defaultIcon,
                textColor = if (isDisabled) disabledText else MaterialTheme.colorScheme.onSurface,
                onClick = { if (!isDisabled) onAction(HomeAction.OnRecordPaymentClicked) }
            )
            QuickActionItem(
                label = "Expense",
                icon = Icons.Outlined.Receipt,
                containerColor = if (isDisabled) disabledContainer else defaultContainer,
                iconColor = if (isDisabled) disabledIcon else defaultIcon,
                textColor = if (isDisabled) disabledText else MaterialTheme.colorScheme.onSurface,
                onClick = { if (!isDisabled) onAction(HomeAction.OnExpenseClicked) }
            )
            QuickActionItem(
                label = "Add tenant",
                icon = Icons.Outlined.PersonAdd,
                containerColor = if (isDisabled) disabledContainer else defaultContainer,
                iconColor = if (isDisabled) disabledIcon else defaultIcon,
                textColor = if (isDisabled) disabledText else MaterialTheme.colorScheme.onSurface,
                onClick = { if (!isDisabled) onAction(HomeAction.OnAddTenantClicked) }
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DashboardCard(state: HomeState) {
    Surface(
        modifier = Modifier.fillMaxWidth().softShadow(shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary, // Solid primary green
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Label row
            Text(
                text = "Collected this month",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )

            // Amount row (Massive Hero Number)
            Text(
                text = "NPR ${state.collectedRent}",
                style = com.gaatho.rent.core.designsystem.monoDataTextStyle(),
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            val progressVal = if (state.totalRent > 0) (state.collectedRent.toFloat() / state.totalRent.toFloat()) else 0f
            LinearProgressIndicator(
                progress = { progressVal },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.onPrimary,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                strokeCap = StrokeCap.Round
            )

            // Bottom stats row
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "72% of NPR ${state.totalRent}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                Text(
                    text = "${state.propertiesCount} properties · ${state.tenantsCount} tenants",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun OverdueAlertBanner(count: Int) {
    com.gaatho.rent.core.ui.components.AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        containerColor = AppColors.WarningContainer,
        contentColor = AppColors.OnWarning
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = AppColors.Warning,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$count tenants overdue by 5+ days",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = AppColors.Warning,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RecentPaymentsEmpty() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Recent activity",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CurrencyRupee,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No payments yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentPaymentsSection(state: HomeState, onAction: (HomeAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent activity",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(
                onClick = { onAction(HomeAction.OnSeeAllPaymentsClicked) },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (state.recentPayments.isEmpty()) {
            Text(
                text = "No recent payments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                state.recentPayments.forEachIndexed { index, payment ->
                    val avatarPair = com.gaatho.rent.core.designsystem.ExtendedColorHex.AvatarPairs[index % com.gaatho.rent.core.designsystem.ExtendedColorHex.AvatarPairs.size]
                    RecentPaymentItemRow(
                        item = payment,
                        avatarColor = Color(avatarPair.first),
                        avatarText = Color(avatarPair.second),
                        onClick = { onAction(HomeAction.OnRecentPaymentClicked(payment.tenantId)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentPaymentItemRow(
    item: RecentPaymentItem,
    avatarColor: Color,
    avatarText: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.tenantName.take(2).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = avatarText
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.tenantName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.propertyName} · ${item.dateLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "+NPR ${item.amount}",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.Success
        )
    }
    }

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun HomeScreenLightPreview() {
    RentManagerTheme(darkTheme = false) {
        HomeContent(
            state = HomeState(
                propertiesCount = 6,
                tenantsCount = 11,
                collectedRent = 184000L,
                totalRent = 255500L,
                overdueTenantsCount = 3,
                recentPayments = kotlinx.collections.immutable.persistentListOf(
                    RecentPaymentItem(
                        tenantId = "1",
                        tenantName = "Sarah Jenkins",
                        propertyName = "Sunset Residency",
                        dateLabel = "yesterday",
                        amount = 18000L,
                        isPaid = true
                    )
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun HomeScreenDarkPreview() {
    RentManagerTheme(darkTheme = true) {
        HomeContent(
            state = HomeState(
                propertiesCount = 6,
                tenantsCount = 11,
                collectedRent = 184000L,
                totalRent = 255500L,
                overdueTenantsCount = 3
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyPreview() {
    RentManagerTheme(darkTheme = false) {
        HomeContent(
            state = HomeState(
                propertiesCount = 0,
                tenantsCount = 0,
                collectedRent = 0L,
                totalRent = 0L,
                overdueTenantsCount = 0,
                recentPayments = kotlinx.collections.immutable.persistentListOf()
            ),
            onAction = {}
        )
    }
}

@Composable
private fun HomeSkeletonLoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(com.gaatho.rent.core.designsystem.Spacing.SectionGap)
    ) {
        // Quick Actions Shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(4) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    com.gaatho.rent.core.ui.components.AppShimmerBox(
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                    )
                    com.gaatho.rent.core.ui.components.AppShimmerBox(
                        modifier = Modifier.width(60.dp).height(12.dp).clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
        
        // Dashboard Card Shimmer
        com.gaatho.rent.core.ui.components.AppShimmerBox(
            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(24.dp))
        )
        
        // Recent Payments Shimmer
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            com.gaatho.rent.core.ui.components.AppShimmerBox(
                modifier = Modifier.width(120.dp).height(20.dp).clip(RoundedCornerShape(4.dp))
            )
            repeat(3) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    com.gaatho.rent.core.ui.components.AppShimmerBox(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        com.gaatho.rent.core.ui.components.AppShimmerBox(
                            modifier = Modifier.width(100.dp).height(16.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        com.gaatho.rent.core.ui.components.AppShimmerBox(
                            modifier = Modifier.width(140.dp).height(12.dp).clip(RoundedCornerShape(4.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    com.gaatho.rent.core.ui.components.AppShimmerBox(
                        modifier = Modifier.width(60.dp).height(20.dp).clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}
