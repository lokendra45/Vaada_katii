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
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.unit.sp
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                        Text("S", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
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
// Top Bar — Now using standard AppTopBar
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// 1. Welcome Section — "Good morning, Sarah"
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WelcomeSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stringResource(Res.string.good_morning),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = stringResource(Res.string.mock_user_name),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Monthly Overview Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MonthlyOverviewCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 0.dp // Flatter design in the mockup
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Label row
            Text(
                text = stringResource(Res.string.collected_rent).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Amount row
            Text(
                text = stringResource(Res.string.mock_collected_amount),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 48.sp // Huge light font per Google Design Philosophy
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Progress bar
            LinearProgressIndicator(
                progress = { 124500f / 450000f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                strokeCap = StrokeCap.Round
            )

            // Stats row (Clean, no clunky grey pill container)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
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
            text = label.replace(":", ""), // Ensure no trailing colons
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

// ─────────────────────────────────────────────────────────────────────────────
// 3. Quick Actions — 4 circular tap targets
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(Res.string.quick_actions),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
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
                icon = Icons.Outlined.Home
            )
            QuickActionItem(
                label = stringResource(Res.string.record_pay_action),
                icon = Icons.Outlined.Payments
            )
            QuickActionItem(
                label = stringResource(Res.string.expense_action),
                icon = Icons.Outlined.Receipt // AttachMoney/Receipt
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
        Box(
            modifier = Modifier
                .size(64.dp) // Large circles
                .clip(CircleShape) // Circle shape as per mockup
                .background(MaterialTheme.colorScheme.primaryContainer), // Crisp Google Blue/Primary tinted background
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(26.dp), // Slightly larger icon for the large circle
                tint = MaterialTheme.colorScheme.primary // Matches the tinted background
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. Recent Payments Section
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(
                onClick = { /* TODO: See all payments */ },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
            ) {
                Text(
                    text = stringResource(Res.string.see_all), // Make sure it says "See all" not "See All" if possible
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Payment rows
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PaymentLedgerRow(
                name = "Suman Shrestha",
                subtitle = "Unit: 13",
                amount = "NPR 1,24,500",
                initials = "JIK",
                avatarBg = Color(0xFFD0D5FA), // Light purple/blue
                avatarText = Color(0xFF333333)
            )
            PaymentLedgerRow(
                name = "Suman Shrestha",
                subtitle = "Unit: 13",
                amount = "NPR 14,500",
                initials = "AH",
                avatarBg = Color(0xFFC4E8C2), // Light green
                avatarText = Color(0xFF333333)
            )
            PaymentLedgerRow(
                name = "Suman Shrestha",
                subtitle = "Unit: 14",
                amount = "NPR 7,500",
                initials = "OS",
                avatarBg = Color(0xFFF9C6C1), // Light red/orange
                avatarText = Color(0xFF333333)
            )
        }
    }
}

@Composable
private fun PaymentLedgerRow(
    name: String,
    subtitle: String,
    amount: String,
    initials: String,
    avatarBg: Color,
    avatarText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // No horizontal padding to match edge-to-edge look in mockup
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp) // Larger avatar to match mockup
                .clip(CircleShape)
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                color = avatarText
            )
        }

        // Name + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Amount
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
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
