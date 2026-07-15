package com.gaatho.rent

import androidx.compose.runtime.Composable
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.navigation.AppNavigation

/**
 * Root composable entry point for the entire application.
 *
 * Responsibilities:
 * 1. Apply the app-wide [RentManagerTheme]
 * 2. Delegate all navigation to [AppNavigation]
 *
 * [AppNavigation] owns the Navigation 3 back stack and routes to the
 * correct screen for each destination. It is completely decoupled from
 * this composable — App.kt stays minimal.
 */
@Composable
fun App() {
    RentManagerTheme {
        AppNavigation()
    }
}
