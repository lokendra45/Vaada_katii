package com.gaatho.rent

import androidx.compose.runtime.Composable
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.environment.AppEnvironment
import com.gaatho.rent.core.navigation.AppNavigation
import com.gaatho.rent.core.security.presentation.components.BiometricGate

/**
 * Root composable entry point for the entire application.
 *
 * Responsibilities:
 * 1. Apply the app-wide [AppEnvironment] to manage locale and theme overriding.
 * 2. Apply the app-wide [RentManagerTheme].
 * 3. Delegate all navigation to [AppNavigation].
 *
 * **Startup logic** (session resolution, guest provisioning, database seeding) is
 * fully owned by [SplashViewModel] and runs inside the Splash screen — keeping this
 * file minimal and free of lifecycle side-effects.
 */
@Composable
fun App() {
    AppEnvironment {
        RentManagerTheme {
            BiometricGate {
                AppNavigation()
            }
        }
    }
}
