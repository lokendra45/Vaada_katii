package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppBadgeType {
    SUCCESS, WARNING, ERROR, INFO, NEUTRAL, BRAND
}

@Composable
fun AppBadge(
    text: String,
    type: AppBadgeType = AppBadgeType.NEUTRAL,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (type) {
        AppBadgeType.SUCCESS -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        AppBadgeType.WARNING -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        AppBadgeType.ERROR -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        AppBadgeType.INFO -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        AppBadgeType.BRAND -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        AppBadgeType.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    AppStatusBadge(
        label = text.uppercase(),
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
        shape = RoundedCornerShape(100),
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp,
        horizontalPadding = 8.dp,
        verticalPadding = 3.dp
    )
}
