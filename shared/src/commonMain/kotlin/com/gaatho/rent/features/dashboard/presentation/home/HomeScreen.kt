package com.gaatho.rent.features.dashboard.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddHome
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.Radius
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.Spacing
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*

// ─────────────────────────────────────────────────────────────────────────────
// Root Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { HomeDashboardTopBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Spacing.ScreenPadding,
                end = Spacing.ScreenPadding,
                top = Spacing.StackLoose,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.SectionGap)
        ) {
            item { WelcomeSection() }
            item { MonthlyOverviewCard() }
            item { QuickActionsSection() }
            item { RecentPaymentsSection() }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeDashboardTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = Spacing.ScreenPadding, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar + App Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = stringResource(Res.string.app_name_dashboard),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Search Icon
        IconButton(
            onClick = { /* TODO: Search */ },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(Res.string.search),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. Welcome Section — "Good morning, Sarah"
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WelcomeSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(Res.string.mock_date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.good_morning),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(Res.string.mock_user_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        // Add button from Screenshot 1
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.add),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Monthly Overview Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MonthlyOverviewCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.ItemGap),
            verticalArrangement = Arrangement.spacedBy(Spacing.ItemGap)
        ) {
            // Label
            Text(
                text = stringResource(Res.string.collected_rent),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Amount row
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.mock_collected_amount),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(Res.string.mock_expected_amount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            // Progress bar — Indigo accent
            LinearProgressIndicator(
                progress = { 124500f / 450000f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewStatItem(label = stringResource(Res.string.outstanding), value = stringResource(Res.string.mock_outstanding_amount))
                OverviewStatItem(label = stringResource(Res.string.properties_label), value = "24")
                OverviewStatItem(label = stringResource(Res.string.tenants_label), value = "23")
            }
        }
    }
}

@Composable
private fun OverviewStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

    // Removed divider for cleaner look matching screenshot 1

// ─────────────────────────────────────────────────────────────────────────────
// 3. Quick Actions — 4 circular tap targets
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.ItemGap)) {
        Text(
            text = stringResource(Res.string.quick_actions),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionItem(
                label = stringResource(Res.string.add_tenant_action),
                icon = Icons.Outlined.PersonAdd
            )
            QuickActionItem(
                label = stringResource(Res.string.add_property_action),
                icon = Icons.Outlined.AddHome
            )
            QuickActionItem(
                label = stringResource(Res.string.record_pay_action),
                icon = Icons.Outlined.Payments
            )
            QuickActionItem(
                label = stringResource(Res.string.expense_action),
                icon = Icons.Outlined.Receipt
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 56×56dp circle button
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. Recent Payments Section — Financial ledger style
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecentPaymentsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.recent_payments_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
                onClick = { /* TODO: See all payments */ },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
            ) {
                Text(
                    text = stringResource(Res.string.see_all_action),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Payment rows
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 0.dp
        ) {
            Column {
                PaymentLedgerRow(
                    name = "John Doe",
                    subtitle = "Unit 4B • May 12",
                    amount = "+Rs. 24,500",
                    status = "Paid",
                    initials = "JD",
                    avatarBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                    avatarText = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                PaymentLedgerRow(
                    name = "Alice Smith",
                    subtitle = "24 Maple St • May 10",
                    amount = "+Rs. 18,000",
                    status = "Paid",
                    initials = "AS",
                    avatarBg = MaterialTheme.colorScheme.tertiary,
                    avatarText = MaterialTheme.colorScheme.onTertiary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                PaymentLedgerRow(
                    name = "Mike Wang",
                    subtitle = "Unit 12A • May 08",
                    amount = "+Rs. 31,000",
                    status = "Paid",
                    initials = "MW",
                    avatarBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                    avatarText = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun PaymentLedgerRow(
    name: String,
    subtitle: String,
    amount: String,
    status: String,
    initials: String,
    avatarBg: Color,
    avatarText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelMedium,
                color = avatarText
            )
        }

        // Name + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Amount + status badge
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall,
                color = AppColors.Success
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = AppColors.SuccessContainer,
                        shape = RoundedCornerShape(Radius.Sm)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.OnSuccess
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun HomeScreenLightPreview() {
    RentManagerTheme(darkTheme = false) {
        HomeScreen()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun HomeScreenDarkPreview() {
    RentManagerTheme(darkTheme = true) {
        HomeScreen()
    }
}
