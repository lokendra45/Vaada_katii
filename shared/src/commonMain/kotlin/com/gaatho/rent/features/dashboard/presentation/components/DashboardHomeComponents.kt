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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.AppShadow.figmaButtonShadow
import com.gaatho.rent.core.designsystem.AppShadow.figmaHeroShadow
import com.gaatho.rent.core.ui.components.AmountRow
import com.gaatho.rent.core.ui.components.AmountText
import com.gaatho.rent.core.ui.components.AppListItemSurface
import com.gaatho.rent.core.ui.components.AppSectionHeader
import com.gaatho.rent.core.ui.components.BodySmallText
import com.gaatho.rent.core.ui.components.BodyText
import com.gaatho.rent.core.ui.components.CaptionText
import com.gaatho.rent.core.ui.components.CardTitle
import com.gaatho.rent.core.ui.components.LabelText
import com.gaatho.rent.core.ui.components.MicroText
import com.gaatho.rent.core.ui.components.SectionTitle
import com.gaatho.rent.core.utils.TenantUtils
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.add_tenant
import rentmanagerapp.shared.generated.resources.currency_npr
import rentmanagerapp.shared.generated.resources.dashboard_alert_circle
import rentmanagerapp.shared.generated.resources.dashboard_avatar
import rentmanagerapp.shared.generated.resources.dashboard_bell
import rentmanagerapp.shared.generated.resources.dashboard_building
import rentmanagerapp.shared.generated.resources.dashboard_credit_card
import rentmanagerapp.shared.generated.resources.dashboard_default_name
import rentmanagerapp.shared.generated.resources.dashboard_namaste
import rentmanagerapp.shared.generated.resources.dashboard_no_recent_payments
import rentmanagerapp.shared.generated.resources.dashboard_no_recent_payments_subtitle
import rentmanagerapp.shared.generated.resources.dashboard_paid
import rentmanagerapp.shared.generated.resources.dashboard_plus
import rentmanagerapp.shared.generated.resources.dashboard_recent_activity
import rentmanagerapp.shared.generated.resources.dashboard_reminder
import rentmanagerapp.shared.generated.resources.dashboard_reminder_bell
import rentmanagerapp.shared.generated.resources.dashboard_target_prefix
import rentmanagerapp.shared.generated.resources.dashboard_this_month_badge
import rentmanagerapp.shared.generated.resources.dashboard_users
import rentmanagerapp.shared.generated.resources.payment_status_overdue
import rentmanagerapp.shared.generated.resources.properties_label
import rentmanagerapp.shared.generated.resources.quick_actions
import rentmanagerapp.shared.generated.resources.record_pay_action
import rentmanagerapp.shared.generated.resources.see_all_action
import rentmanagerapp.shared.generated.resources.tenants_label
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
                CaptionText(
                    text = stringResource(Res.string.dashboard_namaste),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SectionTitle(
                    text = if (userName.isNotBlank()) userName else stringResource(Res.string.dashboard_default_name),
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
                DashboardPillBadge(text = stringResource(Res.string.dashboard_this_month_badge))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AmountText(
                        text = stringResource(Res.string.currency_npr) + " " + formatNpr(collectedRent),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    BodyText(
                        text = stringResource(Res.string.dashboard_target_prefix) + stringResource(Res.string.currency_npr) + " " + formatNpr(totalRent),
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
                label = stringResource(Res.string.properties_label),
                iconColor = AppColors.EmeraldAccent
            )
        )
        DashboardMetricCard(
            modifier = Modifier.weight(1f),
            metric = DashboardMetricUi(
                icon = Res.drawable.dashboard_users,
                value = tenantsCount.toString(),
                label = stringResource(Res.string.tenants_label),
                iconColor = AppColors.EmeraldAccent
            )
        )
        DashboardMetricCard(
            modifier = Modifier.weight(1f),
            metric = DashboardMetricUi(
                icon = Res.drawable.dashboard_alert_circle,
                value = overdueTenantsCount.toString(),
                label = stringResource(Res.string.payment_status_overdue),
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
        AppSectionHeader(title = stringResource(Res.string.quick_actions))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardActionButton(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.add_tenant),
                icon = Res.drawable.dashboard_plus,
                containerColor = AppColors.EmeraldAccent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                outlined = false,
                onClick = onAddTenant
            )
            DashboardActionButton(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.record_pay_action),
                icon = Res.drawable.dashboard_credit_card,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconColor = AppColors.EmeraldAccent,
                outlined = true,
                onClick = onRecordPayment
            )
            DashboardActionButton(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.dashboard_reminder),
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
            AppSectionHeader(title = stringResource(Res.string.dashboard_recent_activity))
                LabelText(
                    text = stringResource(Res.string.see_all_action),
                    modifier = Modifier.clickable(onClick = onSeeAll),
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
private fun DashboardPillBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = AppColors.EmeraldAccentLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.EmeraldAccentBorder)
    ) {
            MicroText(
                text = text,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
            CardTitle(
                text = "$percent%",
                color = MaterialTheme.colorScheme.onSurface
            )
            MicroText(
                text = stringResource(Res.string.dashboard_paid),
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
    // heightIn(min) instead of fixed height — prevents label text from being clipped
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
            CardTitle(
                text = metric.value,
                color = MaterialTheme.colorScheme.onSurface
            )
            BodySmallText(
                text = metric.label,
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
            LabelText(
                text = label,
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
                        tint = AppColors.EmeraldAccent
                    )
                } else {
                    LabelText(
                        text = TenantUtils.getInitials(item.title),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                CardTitle(
                    text = item.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                BodySmallText(
                    text = item.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.amount > 0L) {
                AmountRow(
                    amount = (if (item.isPositive) "+ " else "") + stringResource(Res.string.currency_npr) + " " + formatNpr(item.amount),
                    subtitle = item.dateLabel,
                    amountColor = if (item.isPositive) AppColors.EmeraldAccent else AppColors.Error
                )
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
