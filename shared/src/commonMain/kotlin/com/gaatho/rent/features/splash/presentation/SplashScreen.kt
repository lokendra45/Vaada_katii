package com.gaatho.rent.features.splash.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.splash_app_name
import rentmanagerapp.shared.generated.resources.splash_error_subtitle
import rentmanagerapp.shared.generated.resources.splash_error_title
import rentmanagerapp.shared.generated.resources.splash_retry
import rentmanagerapp.shared.generated.resources.splash_tagline

// ─── Stateful Container ───────────────────────────────────────────────────────

/**
 * Stateful Splash container. Owns the [SplashViewModel], collects state and side-effects,
 * and delegates all rendering to the stateless [SplashContent].
 *
 * @param onNavigateToHome Called when an existing session is found.
 * @param onNavigateToLogin Called when no session exists.
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val viewModel: SplashViewModel = koinViewModel()
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is SplashSideEffect.NavigateToHome -> onNavigateToHome()
            is SplashSideEffect.NavigateToLogin -> onNavigateToLogin()
        }
    }

    SplashContent(
        phase = state.phase,
        onRetry = { viewModel.onAction(SplashAction.Retry) },
    )
}

// ─── Stateless UI ────────────────────────────────────────────────────────────

/**
 * Stateless UI for the Splash screen.
 *
 * Features:
 * - Animated logo entrance (fade + scale in on first composition)
 * - Pulsing progress indicator while loading
 * - Error + retry state with graceful [AnimatedContent] crossfade
 * - All strings sourced from [Res.string.*] — zero hardcoded copy
 */
@Composable
fun SplashContent(
    phase: SplashState.Phase = SplashState.Phase.Loading,
    onRetry: () -> Unit = {},
) {
    // Logo entrance animation — runs once on first composition
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.coroutineScope {
            launch { logoAlpha.animateTo(1f, animationSpec = tween(600, easing = EaseOutCubic)) }
            launch { logoScale.animateTo(1f, animationSpec = tween(600, easing = EaseOutCubic)) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {

        // ── Branding (center) ──────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(logoAlpha.value)
                .scale(logoScale.value),
        ) {
            Text(
                text = stringResource(Res.string.splash_app_name),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                ),
            )
        }

        // ── Bottom section: loading indicator ↔ error state ──────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                targetState = phase,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                },
                label = "SplashPhase",
            ) { currentPhase ->
                when (currentPhase) {
                    is SplashState.Phase.Loading -> LoadingIndicator()
                    is SplashState.Phase.Error -> ErrorSection(onRetry = onRetry)
                }
            }
        }
    }
}

// ─── Sub-composables ─────────────────────────────────────────────────────────

@Composable
private fun LoadingIndicator() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Pulsing progress bar
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val progressAlpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "progressAlpha",
        )

        LinearProgressIndicator(
            modifier = Modifier
                .width(120.dp)
                .height(2.dp)
                .clip(CircleShape)
                .alpha(progressAlpha),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.splash_tagline),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorSection(onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 40.dp),
    ) {
        Text(
            text = stringResource(Res.string.splash_error_title),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.splash_error_subtitle),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(
                text = stringResource(Res.string.splash_retry),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun SplashLoadingPreview() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        SplashContent(phase = SplashState.Phase.Loading)
    }
}

@Preview
@Composable
private fun SplashErrorPreview() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        SplashContent(phase = SplashState.Phase.Error("Connection timeout"))
    }
}
