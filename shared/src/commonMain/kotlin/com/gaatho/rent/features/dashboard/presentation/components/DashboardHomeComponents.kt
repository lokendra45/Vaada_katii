package com.gaatho.rent.features.dashboard.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.AppShadow.figmaButtonShadow
import com.gaatho.rent.core.designsystem.AppShadow.figmaCardShadow
import com.gaatho.rent.core.designsystem.AppShadow.figmaHeroShadow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.dashboard_alert_circle
import rentmanagerapp.shared.generated.resources.dashboard_avatar
import rentmanagerapp.shared.generated.resources.dashboard_bell
import rentmanagerapp.shared.generated.resources.dashboard_building
import rentmanagerapp.shared.generated.resources.dashboard_credit_card
import rentmanagerapp.shared.generated.resources.dashboard_plus
import rentmanagerapp.shared.generated.resources.dashboard_reminder_bell
import rentmanagerapp.shared.generated.resources.dashboard_users
import kotlin.math.roundToInt

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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.dashboard_avatar),
                contentDescription = if (userName.isBlank()) "Profile" else userName,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Namaste, \uD83D\uDC4B",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (userName.isNotBlank()) userName else "Ramesh ji",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        val shape = CircleShape
        Surface(
            modifier = Modifier
                .size(40.dp)
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
                    modifier = Modifier.size(20.dp)
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
    val progress = if (totalRent > 0) {
        (collectedRent.toFloat() / totalRent.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardPillBadge(text = "THIS MONTH")
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "NPR ${formatNpr(collectedRent)}",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Target: NPR ${formatNpr(totalRent)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DashboardProgressRing(
                progress = progress,
                percent = (progress * 100f).roundToInt()
            )
        }
    }
}

@Composable
fun DashboardMetricsRow(
    propertiesCount: Int,
    tenantsCount: Int,
    overdueTenantsCount: Int,
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
                label = "Properties",
                iconColor = AppColors.EmeraldAccent
            )
        )
        DashboardMetricCard(
            modifier = Modifier.weight(1f),
            metric = DashboardMetricUi(
                icon = Res.drawable.dashboard_users,
                value = tenantsCount.toString(),
                label = "Tenants",
                iconColor = AppColors.EmeraldAccent
            )
        )
        DashboardMetricCard(
            modifier = Modifier.weight(1f),
            metric = DashboardMetricUi(
                icon = Res.drawable.dashboard_alert_circle,
                value = overdueTenantsCount.toString(),
                label = "Overdue",
                iconColor = AppColors.Error
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
        DashboardSectionHeader(title = "Quick Actions")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardActionButton(
                modifier = Modifier.weight(1f),
                label = "Add Tenant",
                icon = Res.drawable.dashboard_plus,
                containerColor = AppColors.EmeraldAccent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                outlined = false,
                onClick = onAddTenant
            )
            DashboardActionButton(
                modifier = Modifier.weight(1f),
                label = "Record Pay",
                icon = Res.drawable.dashboard_credit_card,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconColor = AppColors.EmeraldAccent,
                outlined = true,
                onClick = onRecordPayment
            )
            DashboardActionButton(
                modifier = Modifier.weight(1f),
                label = "Reminder",
                icon = Res.drawable.dashboard_reminder_bell,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconColor = AppColors.Warning,
                outlined = true,
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
            DashboardSectionHeader(title = "Recent Activity")
            Text(
                text = "See All",
                modifier = Modifier.clickable(onClick = onSeeAll),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = AppColors.EmeraldAccent
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
                            0 -> AppColors.AvatarNeutral
                            1 -> AppColors.AvatarSuccess
                            else -> AppColors.AvatarError
                        },
                        onClick = { onActivityClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSectionHeader(title: String) {
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
        color = AppColors.EmeraldAccentLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.EmeraldAccentBorder)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = AppColors.EmeraldAccentDark
        )
    }
}

@Composable
private fun DashboardProgressRing(progress: Float, percent: Int) {
    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(
                color = AppColors.ProgressTrack,
                radius = size.minDimension / 2f,
                style = stroke
            )
            drawArc(
                color = AppColors.EmeraldAccent,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "PAID",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
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
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .height(90.dp)
            .figmaCardShadow(shape = shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 0.dp
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
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
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
    outlined: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = contentColor
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier
            .height(44.dp)
            .figmaButtonShadow(shape = shape, prominent = !outlined)
            .clickable(onClick = onClick),
        shape = shape,
        color = containerColor,
        border = if (outlined) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardResourceIcon(
                resource = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
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
            title = "No recent payments",
            subtitle = "New rent activity will appear here",
            amount = 0L,
            dateLabel = "",
            isPositive = true
        ),
        avatarColor = AppColors.AvatarSuccess,
        onClick = {}
    )
}

@Composable
private fun DashboardActivityCard(
    item: DashboardActivityUi,
    avatarColor: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .figmaCardShadow(shape = shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
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
                        tint = AppColors.EmeraldAccent
                    )
                } else {
                    Text(
                        text = initialsFor(item.title),
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
                        text = "${if (item.isPositive) "+ " else ""}NPR ${formatNpr(item.amount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (item.isPositive) AppColors.EmeraldAccent else AppColors.Error,
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

private fun initialsFor(name: String): String {
    val words = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
        words.size == 1 -> words[0].take(2).uppercase()
        else -> "GB"
    }
}
