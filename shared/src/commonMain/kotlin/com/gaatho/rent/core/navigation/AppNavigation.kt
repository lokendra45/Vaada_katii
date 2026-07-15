package com.gaatho.rent.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.features.auth.presentation.PhoneOtpLoginScreen
import com.gaatho.rent.features.auth.presentation.VerifyOtpScreen
import com.gaatho.rent.features.property.presentation.list.PropertyListScreen
import com.gaatho.rent.features.splash.presentation.SplashScreen
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json.Default.serializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import com.gaatho.rent.features.dashboard.presentation.MainDashboardScreen
import org.koin.compose.koinInject

/**
 * Root navigation graph for the entire application.
 *
 * ## Architecture
 *
 * Navigation 3 uses a **user-owned backStack** (`SnapshotStateList<Route>`).
 * The UI owns the backStack — the ViewModel does NOT touch any navigator.
 * This is the correct separation of concerns:
 *
 * ```
 * ViewModel      →  postSideEffect(NavigateToDetails(id))
 * Screen         →  collectSideEffect { onNavigateToDetails(id) }
 * AppNavigation  →  receives lambda, mutates backStack
 * ```
 *
 * ## Serialization (CMP requirement)
 * iOS and WASM cannot use JVM reflection. Navigation 3 requires kotlinx.serialization
 * via [SavedStateConfiguration]. `subclassesOfSealed<Route>()` registers every
 * `@Serializable` subclass of [Route] automatically — no manual upkeep.
 *
 * ## Key import paths for Navigation 3 CMP v1.1.1
 * - `SavedStateConfiguration`        → `androidx.navigation3.ui`
 * - `rememberNavBackStack`            → `androidx.navigation3.ui`
 * - `NavKey`                          → `androidx.navigation3.runtime`
 * - `entry`, `entryProvider`          → `androidx.navigation3.runtime`
 * - `rememberViewModelStoreNavEntryDecorator` → `androidx.lifecycle.viewmodel.navigation3`
 *
 * ## Adding a new screen
 * 1. Add `@Serializable data object/class YourRoute : Route` in [Routes.kt]
 * 2. Add `entry<YourRoute> { YourScreen() }` in [entryProvider] below
 */

/** Serialization config for all [Route] subclasses — registered via sealed hierarchy. */
@OptIn(ExperimentalSerializationApi::class)
private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Route>()
        }
    }
}

/**
 * Root composable that owns the app back stack and displays the correct screen
 * for each [Route].
 *
 * **Start destination**: [SplashRoute] — validates session & routes to [PropertyListRoute] or [PhoneOtpLoginRoute].
 */
@Composable
fun AppNavigation() {
    val sessionManager: SessionManager = koinInject()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

    val backStack = rememberNavBackStack(navConfig, SplashRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            // Scopes a ViewModel per navigation entry (cleared when entry is popped)
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            // ── Splash & Startup ──────────────────────────────────────────────

            entry<SplashRoute> {
                SplashScreen(
                    onNavigateToHome = {
                        backStack.clear()
                        backStack.add(MainDashboardRoute)
                    },
                    onNavigateToLogin = {
                        backStack.clear()
                        backStack.add(PhoneOtpLoginRoute)
                    }
                )
            }

            // ── Dashboard Shell ───────────────────────────────────────────────

            entry<MainDashboardRoute> {
                MainDashboardScreen(
                    onNavigateToPropertyDetails = { propertyId ->
                        backStack.add(PropertyDetailRoute(propertyId))
                    },
                    onNavigateToAddProperty = {
                        backStack.add(AddPropertyRoute)
                    }
                )
            }

            // ── Property ──────────────────────────────────────────────────────

            entry<PropertyListRoute> {
                // Now hosted inside MainDashboardScreen as a tab, but can still be pushed directly if ever needed.
                PropertyListScreen(
                    onNavigateToDetails = { propertyId ->
                        backStack.add(PropertyDetailRoute(propertyId))
                    },
                    onNavigateToAddProperty = {
                        backStack.add(AddPropertyRoute)
                    }
                )
            }

            entry<PropertyDetailRoute> { route ->
                // TODO: Replace with PropertyDetailScreen(propertyId = route.propertyId)
                PlaceholderScreen("Property Detail\n${route.propertyId}")
            }

            entry<AddPropertyRoute> {
                // TODO: Replace with AddPropertyScreen()
                PlaceholderScreen("Add Property")
            }

            entry<EditPropertyRoute> { route ->
                // TODO: Replace with EditPropertyScreen(propertyId = route.propertyId)
                PlaceholderScreen("Edit Property\n${route.propertyId}")
            }

            entry<UnitListRoute> { route ->
                // TODO: Replace with UnitListScreen(propertyId = route.propertyId)
                PlaceholderScreen("Units for\n${route.propertyId}")
            }

            // ── Auth (Phone OTP & Dual Role) ──────────────────────────────────

            entry<PhoneOtpLoginRoute> {
                PhoneOtpLoginScreen(
                    onNavigateToVerifyOtp = { phone, role ->
                        backStack.add(VerifyOtpRoute(phone, role))
                    },
                    onNavigateToHome = {
                        backStack.clear()
                        backStack.add(MainDashboardRoute)
                    }
                )
            }

            entry<VerifyOtpRoute> { route ->
                VerifyOtpScreen(
                    phoneNumber = route.phoneNumber,
                    selectedRole = route.selectedRole,
                    onNavigateToHome = {
                        backStack.clear()
                        backStack.add(MainDashboardRoute)
                    },
                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}

/**
 * Temporary placeholder for screens not yet implemented.
 * Replace each call site with the real screen composable as each feature is built.
 */
@Composable
private fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label)
    }
}
