package com.gaatho.rent.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * A highly performant, reusable shimmer effect modifier.
 *
 * Uses [drawWithCache] to ensure the gradient Brush is only instantiated once
 * and simply translated during the animation loop. This avoids the heavy recomposition
 * and layout passes that occur when using `Modifier.background()` with an animated brush.
 *
 * @param durationMillis Duration of the sweep animation in milliseconds.
 * @param showShimmer If false, the modifier does nothing (useful for toggling state).
 */
fun Modifier.shimmer(
    durationMillis: Int = 1200,
    showShimmer: Boolean = true
): Modifier = composed {
    if (!showShimmer) return@composed this

    // We use the theme's surface color as the base, and slightly lighter/darker shades for the sweep.
    val baseColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    val highlightColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val shimmerColors = listOf(baseColor, highlightColor, baseColor)

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    this.drawWithCache {
        // Calculate the translation based on the size of the drawing area.
        // We sweep from top-left to bottom-right.
        val width = size.width
        val height = size.height
        val totalTranslation = width + height
        
        // Map the 0..1000 value to actual pixels
        val progress = translateAnim / 1000f
        val offset = totalTranslation * progress - width

        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(x = offset, y = offset),
            end = Offset(x = offset + width, y = offset + height)
        )

        onDrawBehind {
            drawRect(brush = brush)
        }
    }
}

/**
 * A convenient Composable that renders a shimmering placeholder box.
 * Perfect for dropping into loading skeletons.
 */
@Composable
fun AppShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .shimmer()
    )
}
