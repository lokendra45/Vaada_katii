package com.gaatho.rent.core.security.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.security.presentation.SecurityViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * A wrapper composable that locks the app behind biometric authentication
 * if the user has enabled it in settings.
 */
@Composable
fun BiometricGate(
    content: @Composable () -> Unit
) {
    val viewModel: SecurityViewModel = koinViewModel()
    val isLocked by viewModel.isLocked.collectAsState()
    val isNotEnrolled by viewModel.isNotEnrolled.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.authError.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Trigger auth when the locked screen is shown
    LaunchedEffect(isLocked, isNotEnrolled) {
        if (isLocked && !isNotEnrolled) {
            viewModel.authenticate()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main App Content (Blurred when locked)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isLocked) Modifier.blur(20.dp) else Modifier)
        ) {
            content()
        }

        // Lock Screen Overlay
        AnimatedVisibility(
            visible = isLocked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (isNotEnrolled) "Setup Required" else "App Locked",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isNotEnrolled) 
                            "Biometrics are enabled but no fingerprint is enrolled on this device. Please set it up in system settings."
                            else "Authentication is required to access your property data.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    if (isNotEnrolled) {
                        Button(
                            onClick = { viewModel.onEnrollClicked() },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = "Go to Settings",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.authenticate() },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Unlock with Fingerprint",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}
