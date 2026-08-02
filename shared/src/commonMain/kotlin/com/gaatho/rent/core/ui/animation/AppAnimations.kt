package com.gaatho.rent.core.ui.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

/**
 * Buttery-smooth spring-based slide transition — matches GaathoMobileApp's premium feel.
 *
 * Uses:
 * - Spring physics instead of tween → natural deceleration, no hard stop
 * - Only 8% horizontal offset → subtle parallax, not a full-screen slam
 * - Fade combined with slide → depth perception
 *
 * @param direction  1 = forward (left→right entry), -1 = backward (right→left entry)
 * @param stiffness  Spring stiffness. Lower = slower/bouncier. Default feels premium.
 */
fun tabSlideTransition(
    direction: Int = 1,
    stiffness: Float = Spring.StiffnessMediumLow
): ContentTransform {
    val offsetSpec = spring<IntOffset>(
        stiffness = stiffness,
        dampingRatio = Spring.DampingRatioNoBouncy,
    )
    val fadeSpec = spring<Float>(stiffness = stiffness)

    return (
        fadeIn(animationSpec = fadeSpec) +
        slideInHorizontally(animationSpec = offsetSpec) { (it * 0.08f * direction).toInt() }
    ) togetherWith (
        fadeOut(animationSpec = fadeSpec) +
        slideOutHorizontally(animationSpec = offsetSpec) { (it * -0.08f * direction).toInt() }
    )
}

/**
 * Fade-only transition — used for tab switches where direction is ambiguous
 * or for modal/overlay screens that shouldn't slide.
 */
fun fadeTransition(stiffness: Float = Spring.StiffnessMediumLow): ContentTransform {
    val spec = spring<Float>(stiffness = stiffness)
    return fadeIn(animationSpec = spec) togetherWith fadeOut(animationSpec = spec)
}
