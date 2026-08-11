package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.softShadow

/**
 * Standardized AppCard replacing all default Material Cards.
 * Features the ultra-soft "floating" shadow and pure white backgrounds 
 * by default (no hard borders).
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium, // Defaults to our new 24.dp organic radius
    containerColor: Color = MaterialTheme.colorScheme.surface, // Pure White
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.softShadow(shape = shape),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 0.dp // We use our custom softShadow instead
    ) {
        androidx.compose.foundation.layout.Column(content = content)
    }
}
