package com.gaatho.rent.core.security.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.security.presentation.SecurityViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.security_app_locked
import rentmanagerapp.shared.generated.resources.security_app_locked_desc
import rentmanagerapp.shared.generated.resources.security_auth_desc
import rentmanagerapp.shared.generated.resources.security_auth_required_title
import rentmanagerapp.shared.generated.resources.security_go_to_settings
import rentmanagerapp.shared.generated.resources.security_not_enrolled_desc
import rentmanagerapp.shared.generated.resources.security_setup_required
import rentmanagerapp.shared.generated.resources.security_unlock_fingerprint

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

    val authTitle = stringResource(Res.string.security_auth_required_title)
    val authDesc = stringResource(Res.string.security_auth_desc)

    LaunchedEffect(Unit) {
        viewModel.authError.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Trigger auth when the locked screen is shown
    LaunchedEffect(isLocked, isNotEnrolled) {
        if (isLocked && !isNotEnrolled) {
            viewModel.authenticate(authTitle, authDesc)
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
                        text = stringResource(
                            if (isNotEnrolled) Res.string.security_setup_required
                            else Res.string.security_app_locked
                        ),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(
                            if (isNotEnrolled) Res.string.security_not_enrolled_desc
                            else Res.string.security_app_locked_desc
                        ),
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
                                text = stringResource(Res.string.security_go_to_settings),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.authenticate(authTitle, authDesc)
                            },
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
                                    text = stringResource(Res.string.security_unlock_fingerprint),
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
