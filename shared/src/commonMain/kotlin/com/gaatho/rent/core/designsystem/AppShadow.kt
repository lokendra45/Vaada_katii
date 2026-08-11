package com.gaatho.rent.core.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a premium, ultra-soft drop shadow to any component.
 * Replaces hard borders with a diffused glow that makes components "float".
 */
fun Modifier.softShadow(
    elevation: Dp = 24.dp,
    shape: androidx.compose.ui.graphics.Shape,
    spotColor: Color = Color(0x0A000000), // 4% Opacity Black
    ambientColor: Color = Color(0x05000000) // 2% Opacity Black
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = false,
    ambientColor = ambientColor,
    spotColor = spotColor
)
