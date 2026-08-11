package com.gaatho.rent.core.ui.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

private val IosSpringStiffness = 300f // Matches standard iOS curve
private val IosSpringDamping = 0.85f  // No bounciness, just smooth stop

/**
 * Native iOS Push Transition (Right to Left)
 * 
 * - Entering screen slides in 100% from the right.
 * - Exiting screen slides -30% to the left and subtly fades (parallax).
 */
fun iosPushTransition(): ContentTransform {
    val offsetSpec = spring<IntOffset>(
        stiffness = IosSpringStiffness,
        dampingRatio = IosSpringDamping
    )
    val fadeSpec = spring<Float>(
        stiffness = IosSpringStiffness,
        dampingRatio = IosSpringDamping
    )

    return (
        slideInHorizontally(animationSpec = offsetSpec) { it } +
        fadeIn(animationSpec = fadeSpec)
    ) togetherWith (
        slideOutHorizontally(animationSpec = offsetSpec) { (it * -0.3f).toInt() } +
        fadeOut(animationSpec = fadeSpec, targetAlpha = 0.5f)
    )
}

/**
 * Native iOS Pop Transition (Left to Right)
 * 
 * - Entering screen slides in from -30% on the left (parallax).
 * - Exiting screen slides out 100% to the right.
 */
fun iosPopTransition(): ContentTransform {
    val offsetSpec = spring<IntOffset>(
        stiffness = IosSpringStiffness,
        dampingRatio = IosSpringDamping
    )
    val fadeSpec = spring<Float>(
        stiffness = IosSpringStiffness,
        dampingRatio = IosSpringDamping
    )

    return (
        slideInHorizontally(animationSpec = offsetSpec) { (it * -0.3f).toInt() } +
        fadeIn(animationSpec = fadeSpec)
    ) togetherWith (
        slideOutHorizontally(animationSpec = offsetSpec) { it } +
        fadeOut(animationSpec = fadeSpec, targetAlpha = 0f)
    )
}

/**
 * Fast, buttery crossfade for bottom navigation tabs.
 * We use tween instead of spring here because tab switching should feel instant,
 * without the decelerating tail of a spring.
 */
fun tabCrossfadeTransition(): ContentTransform {
    return fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
}

/**
 * Fade-only transition — used for modal/overlay screens.
 */
fun fadeTransition(stiffness: Float = Spring.StiffnessMediumLow): ContentTransform {
    val spec = spring<Float>(stiffness = stiffness)
    return fadeIn(animationSpec = spec) togetherWith fadeOut(animationSpec = spec)
}
