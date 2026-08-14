package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import com.gaatho.rent.core.designsystem.Radius
import com.gaatho.rent.core.designsystem.AppShadow.figmaCardShadow

/**
 * Reusable clean list-item surface — the **single source of truth** for every
 * list row in the app (properties, tenants, payments).
 *
 * Provides a flat, clean design with optional click ripple.
 */
@Composable
fun AppListItemSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = if (borderWidth > 0.dp && borderColor != Color.Transparent) BorderStroke(borderWidth, borderColor) else null,
        shadowElevation = shadowElevation
    ) {
        Column(content = content)
    }
}
