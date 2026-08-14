package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.border
import com.gaatho.rent.core.designsystem.AppShadow.figmaCardShadow

/**
 * Standardized Figma-aligned card surface.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium, // Defaults to our new 24.dp organic radius
    containerColor: Color = MaterialTheme.colorScheme.surface, // Pure White
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 0.dp,
    useCardShadow: Boolean = true,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .then(if (useCardShadow) Modifier.figmaCardShadow(shape = shape) else Modifier)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            ),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = shadowElevation
    ) {
        androidx.compose.foundation.layout.Column(content = content)
    }
}
