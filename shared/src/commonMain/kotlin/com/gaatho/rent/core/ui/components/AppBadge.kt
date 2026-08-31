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
        AppBadgeType.SUCCESS -> com.gaatho.rent.core.designsystem.AppColors.SuccessContainer to com.gaatho.rent.core.designsystem.AppColors.Success
        AppBadgeType.WARNING -> com.gaatho.rent.core.designsystem.AppColors.WarningContainer to com.gaatho.rent.core.designsystem.AppColors.Warning
        AppBadgeType.ERROR -> com.gaatho.rent.core.designsystem.AppColors.ErrorContainer to com.gaatho.rent.core.designsystem.AppColors.Error
        AppBadgeType.INFO -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
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
