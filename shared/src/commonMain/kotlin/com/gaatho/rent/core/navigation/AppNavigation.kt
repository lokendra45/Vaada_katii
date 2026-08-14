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
import com.gaatho.rent.core.ui.animation.iosPushTransition
import com.gaatho.rent.core.ui.animation.iosPopTransition
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
import com.gaatho.rent.features.payment.presentation.details.PaymentDetailsScreen
import com.gaatho.rent.features.payment.presentation.edit.EditPaymentScreen
import com.gaatho.rent.features.paywall.presentation.PaywallScreen
import com.gaatho.rent.features.property.presentation.details.PropertyDetailsScreen
import com.gaatho.rent.features.property.presentation.add.AddPropertyScreen
import com.gaatho.rent.features.property.presentation.edit.EditPropertyScreen
import com.gaatho.rent.features.tenant.presentation.add.AddTenantScreen
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantScreen
import com.gaatho.rent.features.tenant.presentation.list.TenantsListScreen
import com.gaatho.rent.features.payment.presentation.list.PaymentListScreen
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
        transitionSpec = { iosPushTransition() },
        popTransitionSpec = { iosPopTransition() },
        entryProvider = entryProvider {

            entry<SplashRoute> {
                SplashScreen(
                    onNavigateToHome = {
                        backStack.clear()
                        backStack.add(MainDashboardRoute)
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
                    onNavigateToAddPayment = {
                        backStack.add(AddPaymentRoute)
                    },
                    onNavigateToPaymentDetails = { paymentId ->
                        backStack.add(PaymentDetailRoute(paymentId))
                    },
                    onNavigateToPaymentList = {
                        backStack.add(PaymentListRoute)
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
                    onNavigateToAddTenant = {
                        backStack.add(AddTenantRoute)
                    }
                )
            }

            entry<AddPropertyRoute> {
                AddPropertyScreen(
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
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
                    },
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<TenantDetailRoute> { route ->
                com.gaatho.rent.features.tenant.presentation.details.TenantDetailsScreen(
                    tenantId = route.tenantId,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToEdit = { tenantId ->
                        backStack.add(EditTenantRoute(tenantId))
                    }
                )
            }

            entry<AddTenantRoute> {
                AddTenantScreen(
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<EditTenantRoute> { route ->
                EditTenantScreen(
                    tenantId = route.tenantId,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<AddPaymentRoute> {
                com.gaatho.rent.features.payment.presentation.add.AddPaymentScreen(
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<PaymentDetailRoute> { route ->
                PaymentDetailsScreen(
                    paymentId = route.paymentId,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToEdit = { paymentId ->
                        backStack.add(EditPaymentRoute(paymentId))
                    }
                )
            }

            entry<EditPaymentRoute> { route ->
                EditPaymentScreen(
                    paymentId = route.paymentId,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
            
            entry<PaymentListRoute> {
                PaymentListScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToPaymentDetails = { paymentId ->
                        backStack.add(PaymentDetailRoute(paymentId))
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
