package com.gaatho.rent.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable Material 3 Expressive Loading Indicator.
 *
 * Uses M3 Expressive morphing shapes (`LoadingIndicator` / `ContainedLoadingIndicator`)
 * with ultra-smooth spring physics and dynamic color container theming.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppExpressiveLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    contained: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    if (contained) {
        ContainedLoadingIndicator(
            modifier = modifier,
            containerColor = containerColor
        )
    } else {
        LoadingIndicator(
            modifier = modifier,
            color = color
        )
    }
}
