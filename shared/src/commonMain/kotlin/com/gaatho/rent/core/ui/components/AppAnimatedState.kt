package com.gaatho.rent.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A highly smooth, iOS-like buttery animation transition for state changes.
 * Used for transitioning between Loading, Error, Empty, and Success states.
 */
@Composable
fun <T> AppAnimatedState(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
        },
        modifier = modifier,
        label = "AppStateAnimation"
    ) { state ->
        content(state)
    }
}
