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
import com.gaatho.rent.features.auth.presentation.LoginScreen
import com.gaatho.rent.features.property.presentation.list.PropertyListScreen
import com.gaatho.rent.features.property.presentation.add.AddPropertyScreen
import com.gaatho.rent.features.splash.presentation.SplashScreen
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json.Default.serializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import com.gaatho.rent.features.dashboard.presentation.MainDashboardScreen
import com.gaatho.rent.features.paywall.presentation.PaywallScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalSerializationApi::class)
private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Route>()
        }
    }
}

@Composable
fun AppNavigation() {
    val sessionManager: SessionManager = koinInject()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

    val backStack = rememberNavBackStack(navConfig, SplashRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            entry<SplashRoute> {
                SplashScreen(
                    onNavigateToHome = {
                        backStack.clear()
                        backStack.add(MainDashboardRoute)
                    },
                    onNavigateToLogin = {
                        backStack.clear()
                        backStack.add(LoginRoute)
                    }
                )
            }

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

            entry<PropertyListRoute> {
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
                PlaceholderScreen("Property Detail\n${route.propertyId}")
            }

            entry<AddPropertyRoute> {
                AddPropertyScreen(
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<EditPropertyRoute> { route ->
                PlaceholderScreen("Edit Property\n${route.propertyId}")
            }

            entry<UnitListRoute> { route ->
                PlaceholderScreen("Units for\n${route.propertyId}")
            }

            entry<PaywallRoute> {
                PaywallScreen(
                    onDismiss = { backStack.removeLastOrNull() },
                    onPurchaseSuccess = {
                        backStack.removeLastOrNull() // pop paywall
                    }
                )
            }

            entry<LoginRoute> {
                LoginScreen(
                    onNavigateToHome = {
                        backStack.clear()
                        backStack.add(MainDashboardRoute)
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
