package com.gaatho.rent.core.navigation

import androidx.navigation3.runtime.NavKey
import com.gaatho.rent.core.auth.UserRole
import kotlinx.serialization.Serializable

/**
 * All navigation destinations in the app, defined as a sealed interface.
 *
 * ## Why sealed interface?
 * The official Jetbrains CMP Navigation 3 docs recommend the **single-module
 * sealed interface** approach for apps where all routes live in one module.
 * Kotlin serialization handles the entire hierarchy automatically — no manual
 * `subclass()` registration needed (we use `subclassesOfSealed<Route>()`).
 *
 * ## Why @Serializable on every route?
 * Navigation 3 CMP requires serializable routes for:
 * 1. **State restoration** — back stack survives process death (via SavedStateHandle)
 * 2. **Cross-platform compatibility** — iOS/WASM cannot use JVM reflection-based
 *    serialization, so explicit kotlinx.serialization is mandatory
 *
 * ## Adding new routes
 * 1. Add a `data object` or `data class` implementing [Route] below
 * 2. Add `@Serializable` — this is the ONLY registration step needed
 * 3. Add a `when` branch in [AppNavigation]
 *
 * No other configuration changes are required (sealed class hierarchy is
 * registered automatically via `subclassesOfSealed<Route>()`).
 */
@Serializable
sealed interface Route : NavKey

// ──────────────────────────────────────────────────────────────────────────────
// Startup & Auth Routes
// ──────────────────────────────────────────────────────────────────────────────

/** The initial splash screen that validates session & checks startup data. */
@Serializable
data object SplashRoute : Route

/** The unified login screen for Email/Google/Guest. */
@Serializable
data object LoginRoute : Route

// ──────────────────────────────────────────────────────────────────────────────
// Property Routes
// ──────────────────────────────────────────────────────────────────────────────

/** Root dashboard container with bottom navigation. */
@Serializable
data object MainDashboardRoute : Route

/** List of properties tab within the dashboard. */
@Serializable
data object PropertyListRoute : Route

/**
 * Detail view for a single property.
 *
 * @property propertyId The ID of the property to display.
 */
@Serializable
data class PropertyDetailRoute(val propertyId: String) : Route

/** Form for adding a new property. */
@Serializable
data object AddPropertyRoute : Route

/**
 * Form for editing an existing property.
 *
 * @property propertyId The ID of the property to edit.
 */
@Serializable
data class EditPropertyRoute(val propertyId: String) : Route

// ──────────────────────────────────────────────────────────────────────────────
// Unit Routes (Future — rooms/flats within a property)
// ──────────────────────────────────────────────────────────────────────────────

/**
 * List of units (rooms/flats) within a property.
 *
 * @property propertyId The parent property ID.
 */
@Serializable
data class UnitListRoute(val propertyId: String) : Route

// ──────────────────────────────────────────────────────────────────────────────
// Paywall Route
// ──────────────────────────────────────────────────────────────────────────────

/** Full-screen paywall shown to free/guest users who attempt to enable cloud sync. */
@Serializable
data object PaywallRoute : Route

// ──────────────────────────────────────────────────────────────────────────────
// Tenant Routes
// ──────────────────────────────────────────────────────────────────────────────

/** List of tenants across portfolio. */
@Serializable
data object TenantListRoute : Route

/** Detail view for a single tenant. */
@Serializable
data class TenantDetailRoute(val tenantId: String) : Route

/** Form for adding/assigning a new tenant. */
@Serializable
data object AddTenantRoute : Route
