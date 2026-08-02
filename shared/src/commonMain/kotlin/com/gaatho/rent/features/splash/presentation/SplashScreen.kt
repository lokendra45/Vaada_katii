package com.gaatho.rent.features.splash.presentation

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * Stateful Container for the Splash screen.
 * Owns [SplashViewModel] instantiation, listens to navigation side effects, and delegates rendering to [SplashContent].
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: SplashViewModel = koinViewModel()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is SplashSideEffect.NavigateToHome -> onNavigateToHome()
            is SplashSideEffect.NavigateToLogin -> onNavigateToLogin()
        }
    }

    SplashContent()
}

/**
 * Stateless UI Content for the Splash screen.
 * Features a rich gradient background, a subtle pulsing animated logo card, and clear Nepali-themed branding.
 */
@Composable
fun SplashContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        // Center KOTHA text
        Text(
            text = "KOTHA",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
        )

        // Bottom section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.LinearProgressIndicator(
                modifier = Modifier
                    .width(120.dp)
                    .height(2.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Property management,\nsimplified.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}


/* --- Compose Previews --- */

@Preview
@Composable
private fun SplashContentDefaultPreview() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        SplashContent()
    }
}
