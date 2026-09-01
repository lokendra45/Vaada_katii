package com.gaatho.rent.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.ui.animation.iosPopTransition
import com.gaatho.rent.core.ui.animation.iosPushTransition
import com.gaatho.rent.core.ui.components.AppConfirmDialog
import com.gaatho.rent.core.ui.components.AppSnackbarHost
import com.gaatho.rent.core.ui.components.AppSnackbarVariant
import com.gaatho.rent.core.ui.components.rememberAppSnackbarState
import com.gaatho.rent.features.auth.presentation.LoginScreen
import com.gaatho.rent.features.dashboard.presentation.MainDashboardScreen
import com.gaatho.rent.features.payment.presentation.details.PaymentDetailsScreen
import com.gaatho.rent.features.payment.presentation.edit.EditPaymentScreen
import com.gaatho.rent.features.payment.presentation.list.PaymentListScreen
import com.gaatho.rent.features.paywall.presentation.PaywallScreen
import com.gaatho.rent.features.property.presentation.add.AddPropertyScreen
import com.gaatho.rent.features.property.presentation.details.PropertyDetailsScreen
import com.gaatho.rent.features.property.presentation.edit.EditPropertyScreen
import com.gaatho.rent.features.property.presentation.list.PropertyListScreen
import com.gaatho.rent.features.splash.presentation.SplashScreen
import com.gaatho.rent.features.tenant.presentation.add.AddTenantScreen
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantScreen
import com.gaatho.rent.features.tenant.presentation.list.TenantsListScreen
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.common_not_now
import rentmanagerapp.shared.generated.resources.nav_sign_in_required
import rentmanagerapp.shared.generated.resources.nav_sign_in_required_body
import rentmanagerapp.shared.generated.resources.sign_in_action

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
    val authState by sessionManager.authState.collectAsState()
    
    val connectivityObserver: com.gaatho.rent.core.network.connectivity.ConnectivityObserver = koinInject()
    val isOnline by connectivityObserver.isConnected.collectAsState(initial = true)
    val snackbarState = rememberAppSnackbarState()
    
    LaunchedEffect(isOnline) {
        if (!isOnline) {
            // Keep showing it indefinitely until isOnline is true
            // Since our AppSnackbarState automatically dismisses after duration,
            // we'll loop it or we could add an indefinite duration.
            // Let's use a very long duration for now.
            snackbarState.show(
                message = "No internet connection. Waiting for network...",
                variant = AppSnackbarVariant.INFO,
                durationMs = 9999999L
            )
        } else {
            snackbarState.dismiss()
        }
    }
    
    // We only treat it as "guest" for the UI lock dialogs if it's explicitly an anonymous state
    val isGuest = authState is com.gaatho.rent.core.auth.AuthState.Anonymous

    val backStack = rememberNavBackStack(navConfig, SplashRoute)

    fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    fun resetBackStack(vararg routes: Route) {
        backStack.clear()
        backStack.addAll(routes)
    }

    var showGuestLoginDialog by remember { mutableStateOf(false) }

    // Guests can only browse the empty app shell — they cannot create or edit any
    // data. Any mutating navigation triggers a login prompt instead.
    fun navigateRequiringAuth(navigate: () -> Unit) {
        if (isGuest) {
            showGuestLoginDialog = true
        } else {
            navigate()
        }
    }

    if (showGuestLoginDialog) {
        AppConfirmDialog(
            icon = Icons.AutoMirrored.Outlined.Login,
            title = stringResource(Res.string.nav_sign_in_required),
            body = stringResource(Res.string.nav_sign_in_required_body),
            confirmText = stringResource(Res.string.sign_in_action),
            dismissText = stringResource(Res.string.common_not_now),
            onConfirm = {
                showGuestLoginDialog = false
                resetBackStack(LoginRoute)
            },
            onDismiss = {
                showGuestLoginDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        NavDisplay(
            backStack = backStack,
            onBack = { popBackStack() },
        entryDecorators = listOf(
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = { iosPushTransition() },
        popTransitionSpec = { iosPopTransition() },
        entryProvider = entryProvider {

            entry<SplashRoute> {
                SplashScreen(
                    onNavigateToHome = {
                        resetBackStack(MainDashboardRoute)
                    },
                    onNavigateToLogin = {
                        resetBackStack(LoginRoute)
                    }
                )
            }

            entry<MainDashboardRoute> {
                MainDashboardScreen(
                    onNavigateToPropertyDetails = { propertyId ->
                        backStack.add(PropertyDetailRoute(propertyId))
                    },
                    onNavigateToAddProperty = {
                        navigateRequiringAuth { backStack.add(AddPropertyRoute) }
                    },
                    onNavigateToTenantDetails = { tenantId ->
                        backStack.add(TenantDetailRoute(tenantId))
                    },
                    onNavigateToAddTenant = {
                        navigateRequiringAuth { backStack.add(AddTenantRoute) }
                    },
                    onNavigateToAddPayment = { tenantId, propertyId ->
                        navigateRequiringAuth { backStack.add(AddPaymentRoute(tenantId, propertyId)) }
                    },
                    onNavigateToPaymentDetails = { paymentId ->
                        backStack.add(PaymentDetailRoute(paymentId))
                    },
                    onNavigateToPaymentList = {
                        backStack.add(PaymentListRoute)
                    },
                    onNavigateToLogin = {
                        resetBackStack(LoginRoute)
                    }
                )
            }

            entry<PropertyListRoute> {
                PropertyListScreen(
                    onNavigateToDetails = { propertyId ->
                        backStack.add(PropertyDetailRoute(propertyId))
                    },
                    onNavigateToAddProperty = {
                        navigateRequiringAuth { backStack.add(AddPropertyRoute) }
                    }
                )
            }

            entry<PropertyDetailRoute> { route ->
                PropertyDetailsScreen(
                    propertyId = route.propertyId,
                    onNavigateBack = { popBackStack() },
                    onNavigateToPropertyList = {
                        resetBackStack(MainDashboardRoute)
                    },
                    onNavigateToEdit = { propertyId ->
                        navigateRequiringAuth { backStack.add(EditPropertyRoute(propertyId)) }
                    },
                    onNavigateToAddTenant = {
                        navigateRequiringAuth { backStack.add(AddTenantRoute) }
                    }
                )
            }

            entry<AddPropertyRoute> {
                AddPropertyScreen(
                    onNavigateBack = { popBackStack() }
                )
            }

            entry<EditPropertyRoute> { route ->
                EditPropertyScreen(
                    propertyId = route.propertyId,
                    onNavigateBack = { popBackStack() },
                    onNavigateToPropertyList = {
                        resetBackStack(MainDashboardRoute)
                    }
                )
            }

            entry<UnitListRoute> { route ->
                PlaceholderScreen("Units for\n${route.propertyId}")
            }

            entry<PaywallRoute> {
                PaywallScreen(
                    onDismiss = { popBackStack() },
                    onPurchaseSuccess = {
                        popBackStack() // pop paywall
                    }
                )
            }

            entry<LoginRoute> {
                LoginScreen(
                    onNavigateToHome = {
                        resetBackStack(MainDashboardRoute)
                    }
                )
            }

            entry<TenantListRoute> {
                TenantsListScreen(
                    onNavigateToDetails = { tenantId ->
                        backStack.add(TenantDetailRoute(tenantId))
                    },
                    onNavigateToAddTenant = {
                        navigateRequiringAuth { backStack.add(AddTenantRoute) }
                    },
                    onNavigateToAddPayment = { tenantId, propertyId ->
                        navigateRequiringAuth { backStack.add(AddPaymentRoute(tenantId, propertyId)) }
                    },
                    onNavigateBack = { popBackStack() }
                )
            }

            entry<TenantDetailRoute> { route ->
                com.gaatho.rent.features.tenant.presentation.details.TenantDetailsScreen(
                    tenantId = route.tenantId,
                    onNavigateBack = { popBackStack() },
                    onNavigateToTenantList = {
                        resetBackStack(MainDashboardRoute, TenantListRoute)
                    },
                    onNavigateToEdit = { tenantId ->
                        navigateRequiringAuth { backStack.add(EditTenantRoute(tenantId)) }
                    }
                )
            }

            entry<AddTenantRoute> {
                AddTenantScreen(
                    onNavigateBack = { popBackStack() }
                )
            }

            entry<EditTenantRoute> { route ->
                EditTenantScreen(
                    tenantId = route.tenantId,
                    onNavigateBack = { popBackStack() },
                    onNavigateToTenantList = {
                        resetBackStack(MainDashboardRoute, TenantListRoute)
                    }
                )
            }

            entry<AddPaymentRoute> { route ->
                com.gaatho.rent.features.payment.presentation.add.AddPaymentScreen(
                    tenantIdArg = route.tenantId,
                    propertyIdArg = route.propertyId,
                    onNavigateBack = { popBackStack() }
                )
            }

            entry<PaymentDetailRoute> { route ->
                PaymentDetailsScreen(
                    paymentId = route.paymentId,
                    onNavigateBack = { popBackStack() },
                    onNavigateToPaymentList = {
                        resetBackStack(MainDashboardRoute, PaymentListRoute)
                    },
                    onNavigateToEdit = { paymentId ->
                        navigateRequiringAuth { backStack.add(EditPaymentRoute(paymentId)) }
                    }
                )
            }

            entry<EditPaymentRoute> { route ->
                EditPaymentScreen(
                    paymentId = route.paymentId,
                    onNavigateBack = { popBackStack() },
                    onNavigateToPaymentList = {
                        resetBackStack(MainDashboardRoute, PaymentListRoute)
                    }
                )
            }
            
            entry<PaymentListRoute> {
                PaymentListScreen(
                    onNavigateBack = { popBackStack() },
                    onNavigateToPaymentDetails = { paymentId ->
                        backStack.add(PaymentDetailRoute(paymentId))
                    }
                )
            }
        }
    )
        
        AppSnackbarHost(
            state = snackbarState,
            modifier = Modifier.align(Alignment.TopCenter),
            alignment = Alignment.TopCenter
        )
    }
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
