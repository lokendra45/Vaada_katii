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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.gaatho.rent.core.ui.animation.tabSlideTransition
import androidx.savedstate.serialization.SavedStateConfiguration
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.features.auth.presentation.LoginScreen
import com.gaatho.rent.features.property.presentation.list.PropertyListScreen
import com.gaatho.rent.features.splash.presentation.SplashScreen
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json.Default.serializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import com.gaatho.rent.features.dashboard.presentation.MainDashboardScreen
import com.gaatho.rent.features.paywall.presentation.PaywallScreen
import com.gaatho.rent.features.property.presentation.details.PropertyDetailsScreen
import com.gaatho.rent.features.property.presentation.edit.EditPropertyScreen
import com.gaatho.rent.features.tenant.presentation.list.TenantsListScreen
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
        transitionSpec = { tabSlideTransition(direction = 1) },
        popTransitionSpec = { tabSlideTransition(direction = -1) },
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
                    },
                    onNavigateToTenantDetails = { tenantId ->
                        backStack.add(TenantDetailRoute(tenantId))
                    },
                    onNavigateToAddTenant = {
                        backStack.add(AddTenantRoute)
                    },
                    onNavigateToLogin = {
                        backStack.clear()
                        backStack.add(LoginRoute)
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
                PropertyDetailsScreen(
                    propertyId = route.propertyId,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToEdit = { propertyId ->
                        backStack.add(EditPropertyRoute(propertyId))
                    },
                    onNavigateToTenantDetails = { tenantId ->
                        backStack.add(TenantDetailRoute(tenantId))
                    }
                )
            }

            entry<AddPropertyRoute> {
                // Now handled as a bottom sheet inside PropertyListScreen
                // No-op for navigation, but keeping route for type safety if needed.
            }

            entry<EditPropertyRoute> { route ->
                EditPropertyScreen(
                    propertyId = route.propertyId,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
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

            entry<TenantListRoute> {
                TenantsListScreen(
                    onNavigateToDetails = { tenantId ->
                        backStack.add(TenantDetailRoute(tenantId))
                    },
                    onNavigateToAddTenant = {
                        backStack.add(AddTenantRoute)
                    }
                )
            }

            entry<TenantDetailRoute> { route ->
                com.gaatho.rent.features.tenant.presentation.details.TenantDetailsScreen(
                    tenantId = route.tenantId,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<AddTenantRoute> {
                PlaceholderScreen("Add Tenant Screen")
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
