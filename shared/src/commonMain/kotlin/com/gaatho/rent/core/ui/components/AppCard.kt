package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standardized Figma-aligned card surface.
 *
 * Delegates to [AppListItemSurface] so the border, shadow, and shape
 * are always rendered through a single code path.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 0.dp,
    @Suppress("UNUSED_PARAMETER") useCardShadow: Boolean = true, // retained for binary compat
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AppListItemSurface(
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        borderColor = borderColor,
        shadowElevation = shadowElevation,
        onClick = onClick,
        content = content
    )
}
