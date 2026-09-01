package com.gaatho.rent.features.dashboard.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.AppShadow.figmaButtonShadow
import com.gaatho.rent.core.designsystem.AppShadow.figmaHeroShadow
import com.gaatho.rent.core.ui.components.AmountText
import com.gaatho.rent.core.ui.components.AppLineChart
import com.gaatho.rent.core.ui.components.AppListItemSurface
import com.gaatho.rent.core.ui.components.CaptionText
import com.gaatho.rent.core.ui.components.LineChartData
import com.gaatho.rent.core.utils.TenantUtils
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.add_property_action
import rentmanagerapp.shared.generated.resources.add_tenant_action
import rentmanagerapp.shared.generated.resources.currency_npr
import rentmanagerapp.shared.generated.resources.dashboard_activity_due
import rentmanagerapp.shared.generated.resources.dashboard_activity_paid
import rentmanagerapp.shared.generated.resources.dashboard_bell
import rentmanagerapp.shared.generated.resources.dashboard_building
import rentmanagerapp.shared.generated.resources.dashboard_collected_pending_subtitle
import rentmanagerapp.shared.generated.resources.dashboard_collection
import rentmanagerapp.shared.generated.resources.dashboard_credit_card
import rentmanagerapp.shared.generated.resources.dashboard_default_name
import rentmanagerapp.shared.generated.resources.dashboard_home_title
import rentmanagerapp.shared.generated.resources.dashboard_last_month
import rentmanagerapp.shared.generated.resources.dashboard_namaste
import rentmanagerapp.shared.generated.resources.dashboard_no_recent_payments
import rentmanagerapp.shared.generated.resources.dashboard_no_recent_payments_subtitle
import rentmanagerapp.shared.generated.resources.dashboard_paid
import rentmanagerapp.shared.generated.resources.dashboard_payments_to_follow_up
import rentmanagerapp.shared.generated.resources.dashboard_pending_rent
import rentmanagerapp.shared.generated.resources.dashboard_properties_section
import rentmanagerapp.shared.generated.resources.dashboard_property_let_count
import rentmanagerapp.shared.generated.resources.dashboard_quick_actions
import rentmanagerapp.shared.generated.resources.dashboard_recent_activity
import rentmanagerapp.shared.generated.resources.dashboard_reminder
import rentmanagerapp.shared.generated.resources.dashboard_reminder_bell
import rentmanagerapp.shared.generated.resources.dashboard_rent_collected
import rentmanagerapp.shared.generated.resources.dashboard_rental_overview
import rentmanagerapp.shared.generated.resources.dashboard_shortcuts
import rentmanagerapp.shared.generated.resources.dashboard_this_month
import rentmanagerapp.shared.generated.resources.dashboard_users
import rentmanagerapp.shared.generated.resources.properties_label
import rentmanagerapp.shared.generated.resources.quick_actions
import rentmanagerapp.shared.generated.resources.record_pay_action
import rentmanagerapp.shared.generated.resources.see_all_action
import rentmanagerapp.shared.generated.resources.tenants_label

data class DashboardMetricUi(
    val icon: DrawableResource,
    val value: String,
    val label: String,
    val iconColor: Color
)

data class DashboardActivityUi(
    val title: String,
    val subtitle: String,
    val amount: Long,
    val dateLabel: String,
    val isPositive: Boolean
)

@Composable
fun DashboardWelcomeHeader(
    userName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CaptionText(
                text = com.gaatho.rent.core.utils.DateTimeUtil.formatDashboardHeaderDate(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.dashboard_namaste),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (userName.isNotBlank()) userName else stringResource(Res.string.dashboard_default_name),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            CaptionText(
                text = stringResource(Res.string.dashboard_rental_overview),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val shape = CircleShape
        Surface(
            modifier = Modifier
                .size(44.dp)
                .figmaButtonShadow(shape = shape),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(contentAlignment = Alignment.Center) {
                DashboardResourceIcon(
                    resource = Res.drawable.dashboard_bell,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardCollectionCard(
    collectedRent: Long,
    totalRent: Long,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .figmaHeroShadow(shape = shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(Res.string.dashboard_pending_rent),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AmountText(
                        text = stringResource(Res.string.currency_npr) + " " + formatNpr(totalRent - collectedRent),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(Res.string.dashboard_payments_to_follow_up, 8), // Assuming mock data
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Icon in circular container
            val iconShape = CircleShape
            Surface(
                modifier = Modifier.size(56.dp),
                shape = iconShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardMetricsRow(
    propertiesCount: Int,
    tenantsCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DashboardMetricCard(
            modifier = Modifier.weight(1f),
            metric = DashboardMetricUi(
                icon = Res.drawable.dashboard_building,
                value = propertiesCount.toString(),
                label = stringResource(Res.string.properties_label),
                iconColor = MaterialTheme.colorScheme.primary
            )
        )
        DashboardMetricCard(
            modifier = Modifier.weight(1f),
            metric = DashboardMetricUi(
                icon = Res.drawable.dashboard_users,
                value = tenantsCount.toString(),
                label = stringResource(Res.string.tenants_label),
                iconColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun DashboardQuickActions(
    onAddTenant: () -> Unit,
    onRecordPayment: () -> Unit,
    onReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DashboardSectionHeaderNew(
            caption = stringResource(Res.string.dashboard_shortcuts),
            title = stringResource(Res.string.quick_actions)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardActionButton(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.add_property_action),
                icon = Res.drawable.dashboard_building,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = onAddTenant // Will route to correct one from MainDashboardScreen
            )
            DashboardActionButton(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.record_pay_action),
                icon = Res.drawable.dashboard_credit_card,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = onRecordPayment
            )
            DashboardActionButton(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.dashboard_reminder),
                icon = Res.drawable.dashboard_reminder_bell,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = onReminder
            )
        }
    }
}

@Composable
fun DashboardRecentActivity(
    activities: List<DashboardActivityUi>,
    onSeeAll: () -> Unit,
    onActivityClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardSectionHeader(title = stringResource(Res.string.dashboard_recent_activity))
            Text(
                text = stringResource(Res.string.see_all_action),
                modifier = Modifier.clickable(onClick = onSeeAll),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (activities.isEmpty()) {
            DashboardActivityEmptyCard()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                activities.take(3).forEachIndexed { index, item ->
                    DashboardActivityCard(
                        item = item,
                        avatarColor = when (index % 3) {
                            0 -> MaterialTheme.colorScheme.surfaceVariant
                            1 -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        },
                        onClick = { onActivityClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardSectionHeaderNew(
    caption: String,
    title: String,
    trailingText: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CaptionText(
            text = caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(start = 2.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DashboardSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun DashboardPillBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun DashboardProgressRing(progress: Float, percent: Int) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(
                color = trackColor,
                radius = size.minDimension / 2f,
                style = stroke
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(Res.string.dashboard_paid),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardMetricCard(
    metric: DashboardMetricUi,
    modifier: Modifier = Modifier
) {
    // heightIn(min) instead of fixed height ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â prevents label text from being clipped
    // on small screens or larger font-scale settings.
    AppListItemSurface(
        modifier = modifier.heightIn(min = 84.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            DashboardResourceIcon(
                resource = metric.icon,
                contentDescription = metric.label,
                tint = metric.iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = metric.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DashboardActionButton(
    label: String,
    icon: DrawableResource,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = contentColor
) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = shape,
        color = containerColor,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    DashboardResourceIcon(
                        resource = icon,
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DashboardActivityEmptyCard() {
    DashboardActivityCard(
        item = DashboardActivityUi(
            title = stringResource(Res.string.dashboard_no_recent_payments),
            subtitle = stringResource(Res.string.dashboard_no_recent_payments_subtitle),
            amount = 0L,
            dateLabel = "",
            isPositive = true
        ),
        avatarColor = MaterialTheme.colorScheme.secondaryContainer,
        onClick = {}
    )
}

@Composable
private fun DashboardActivityCard(
    item: DashboardActivityUi,
    avatarColor: Color,
    onClick: () -> Unit
) {
    // Uses AppListItemSurface for consistent border + shadow across all list items.
    // heightIn(min=64.dp) lets the row grow if text wraps on small screens.
    AppListItemSurface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarColor)
                    .border(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (item.amount == 0L) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = TenantUtils.getInitials(item.title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.amount > 0L) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = (if (item.isPositive) "+ " else "") + stringResource(Res.string.currency_npr) + " " + formatNpr(item.amount),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (item.isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        maxLines = 1
                    )
                    Text(
                        text = item.dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardResourceIcon(
    resource: DrawableResource,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}

private fun formatNpr(amount: Long): String {
    val raw = kotlin.math.abs(amount).toString()
    if (raw.length <= 3) return if (amount < 0) "-$raw" else raw

    val lastThree = raw.takeLast(3)
    val leading = raw.dropLast(3)
        .reversed()
        .chunked(2)
        .joinToString(",")
        .reversed()
    val formatted = "$leading,$lastThree"
    return if (amount < 0) "-$formatted" else formatted
}
@Composable
fun DashboardHeaderV2(
    userName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.dashboard_home_title),
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        val initials = if (userName.isNotBlank()) TenantUtils.getInitials(userName) else "S"
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun DashboardSegmentedControl(
    selectedPeriod: com.gaatho.rent.features.dashboard.presentation.home.DashboardPeriod,
    onPeriodSelected: (com.gaatho.rent.features.dashboard.presentation.home.DashboardPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isThisMonth = selectedPeriod == com.gaatho.rent.features.dashboard.presentation.home.DashboardPeriod.THIS_MONTH
            
            Surface(
                modifier = Modifier.weight(1f).fillMaxSize().clickable { onPeriodSelected(com.gaatho.rent.features.dashboard.presentation.home.DashboardPeriod.THIS_MONTH) },
                shape = RoundedCornerShape(20.dp),
                color = if (isThisMonth) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (isThisMonth) 2.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.dashboard_this_month),
                        style = if (isThisMonth) MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) else MaterialTheme.typography.labelLarge,
                        color = if (isThisMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                modifier = Modifier.weight(1f).fillMaxSize().clickable { onPeriodSelected(com.gaatho.rent.features.dashboard.presentation.home.DashboardPeriod.LAST_MONTH) },
                shape = RoundedCornerShape(20.dp),
                color = if (!isThisMonth) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (!isThisMonth) 2.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.dashboard_last_month),
                        style = if (!isThisMonth) MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) else MaterialTheme.typography.labelLarge,
                        color = if (!isThisMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCollectionCardV2(
    collectedRent: Long,
    totalRent: Long,
    chartData: List<Float> = listOf(20f, 25f, 15f, 30f, 20f, 40f, 50f, 52f),
    periodLabel: String = "This Month",
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            Text(
                text = stringResource(Res.string.dashboard_collection),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth().figmaHeroShadow(shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = stringResource(Res.string.dashboard_rent_collected),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "NPR",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = formatNpr(collectedRent),
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = stringResource(Res.string.dashboard_collected_pending_subtitle, formatNpr(totalRent - collectedRent)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Graph
                Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                    val lineColor = MaterialTheme.colorScheme.primary
                    AppLineChart(
                        data = LineChartData(points = chartData),
                        lineColor = lineColor
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

data class DashboardPropertyUi(
    val id: String,
    val name: String,
    val location: String,
    val imageUrl: String? = null,
    val totalUnits: Int,
    val occupiedUnits: Int
)

@Composable
fun DashboardPropertiesList(
    properties: List<DashboardPropertyUi>,
    onAddProperty: () -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.dashboard_properties_section),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.see_all_action),
                modifier = Modifier.clickable(onClick = onSeeAll).padding(end = 4.dp),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 16.dp)
        ) {
            items(properties.size) { index ->
                val prop = properties[index]
                Surface(
                    modifier = Modifier.width(140.dp).height(160.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!prop.imageUrl.isNullOrBlank()) {
                            com.gaatho.rent.core.ui.components.AppAsyncImage(
                                model = prop.imageUrl,
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // Gradient Overlay for text readability
                        Box(modifier = Modifier.fillMaxSize().background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        ))
                        
                        // Let badge
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.dashboard_property_let_count, prop.occupiedUnits, prop.totalUnits),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Column(
                            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                        ) {
                            Text(
                                text = prop.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = prop.location,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            
            item {
                Surface(
                    modifier = Modifier.width(140.dp).height(160.dp).clickable(onClick = onAddProperty),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.add_property_action),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardQuickActionsV2(
    onRecordPayment: () -> Unit,
    onReminder: () -> Unit,
    onAddTenant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        Text(
            text = stringResource(Res.string.dashboard_quick_actions),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth().figmaHeroShadow(shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                QuickActionRow(
                    icon = Res.drawable.dashboard_credit_card,
                    title = stringResource(Res.string.record_pay_action),
                    iconBgColor = MaterialTheme.colorScheme.primary,
                    onClick = onRecordPayment
                )
                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 64.dp))
                QuickActionRow(
                    icon = Res.drawable.dashboard_users,
                    title = stringResource(Res.string.add_tenant_action),
                    iconBgColor = MaterialTheme.colorScheme.secondary,
                    onClick = onAddTenant
                )
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    icon: DrawableResource,
    title: String,
    iconBgColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = iconBgColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                DashboardResourceIcon(
                    resource = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DashboardRecentActivityV2(
    activities: List<DashboardActivityUi>,
    onSeeAll: () -> Unit,
    onActivityClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.dashboard_recent_activity),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.see_all_action),
                modifier = Modifier.clickable(onClick = onSeeAll).padding(end = 4.dp),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth().figmaHeroShadow(shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                if (activities.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.dashboard_no_recent_payments),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    activities.take(10).forEachIndexed { index, item ->
                        RecentActivityRow(
                            item = item,
                            avatarColor = when (index % 3) {
                                0 -> MaterialTheme.colorScheme.primary
                                1 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.outline
                            },
                            onClick = { onActivityClick(index) }
                        )
                        if (index < activities.size - 1 && index < 9) {
                            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentActivityRow(
    item: DashboardActivityUi,
    avatarColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = avatarColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = TenantUtils.getInitials(item.title),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "NPR " + formatNpr(item.amount),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (item.isPositive) stringResource(Res.string.dashboard_activity_paid, item.dateLabel) else stringResource(Res.string.dashboard_activity_due, item.dateLabel),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = if (item.isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

