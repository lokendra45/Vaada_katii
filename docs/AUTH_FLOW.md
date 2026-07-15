# Rent Manager Nepal — Phone OTP & Dual-Role Authentication Flow

This document outlines the end-to-end architecture and navigation flow for **10-digit Phone Number (`+977`) + 6-digit OTP verification** across Android and iOS using **Kotlin Multiplatform**, **JetBrains Navigation 3**, **Store5**, and **Supabase GoTrue (`signInWith(OTP)`)**.

All legacy `Email + Password` login, sign-up, and password recovery screens have been removed in favor of a fast, secure, unified **Passwordless Phone OTP** model with **Airbnb-style Dual-Role Switching (`Landlord ↔ Tenant`)**.

---

## 1. Complete Phone OTP Authentication Flow (Text Diagram)

```text
[ 1. APP LAUNCH ]
       │
       ▼
[ 2. SPLASH SCREEN (SplashRoute) ]
       │
       ├─► Checks local storage (`SupabaseSessionManager.isLoggedIn`)
       │
       ├─► IF SESSION VALID (User already logged in):
       │        │
       │        ▼
       │   [ 3A. MAIN DASHBOARD (`PropertyListRoute` or `TenantDashboardRoute`) ]
       │         • Reads `current_active_role` from user metadata / state
       │         • If LANDLORD ──► Shows Landlord Dashboard (`PropertyListScreen`)
       │         • If TENANT   ──► Shows Tenant Dashboard (`TenantDashboardScreen`)
       │         • User can toggle mode anytime via Profile Menu: [ 🔄 Switch to Tenant / Landlord ]
       │
       └─► IF NO SESSION (or session expired):
                │
                ▼
           [ 3B. PHONE OTP LOGIN SCREEN (`PhoneOtpLoginRoute`) ]
                │
                ├─► Step 1: User Selects Role Tab: [ 🏠 Landlord ] OR [ 👤 Tenant ]
                │
                ├─► Step 2: User Enters 10-Digit Nepal Phone Number:
                │         • Example input: `9841234567` (App automatically prefixes `+977` under the hood)
                │
                ├─► Step 3: Tap "Send Verification Code" (`onSendOtpClicked`)
                │         • Calls `AuthRepository.signInWithPhoneOtp("+9779841234567", selectedRole)`
                │         • Supabase checks if phone number `+9779841234567` exists:
                │               └─► IF NEW USER (`createUser = true`):
                │                     • Creates user profile in Supabase
                │                     • Attaches `roles: ["LANDLORD"]` or `["TENANT"]`
                │                     • Sets `current_active_role = selectedRole`
                │                     • Sends SMS 6-digit code `[ 0 1 2 3 4 5 ]`
                │               └─► IF EXISTING USER (`+9779841234567` exists):
                │                     • Checks if selectedRole is inside their `roles` array
                │                     • Updates `current_active_role = selectedRole`
                │                     • Sends SMS 6-digit code `[ 0 1 2 3 4 5 ]`
                │
                ▼
           [ 4. VERIFY OTP SCREEN (`VerifyOtpRoute(phone, role)`) ]
                │
                ├─► Displays: "Enter the 6-digit verification code sent to +977 9841234567"
                ├─► User Enters 6-Digit Code (`0 1 2 3 4 5`)
                ├─► Features Resend Timer (`Resend code in 00:59`)
                │
                ├─► Tap "Verify & Continue" (`onVerifyClicked`)
                │         • Calls `AuthRepository.verifyPhoneOtp("+9779841234567", "012345")`
                │
                ├─► ON SUCCESS:
                │         • Supabase Auth validates token & issues `access_token` (JWT)
                │         • `SupabaseSessionManager.isLoggedIn` reactive StateFlow emits `true`
                │         • `AppNavigation` clears back stack and lands smoothly on **Main Dashboard** (`3A`)!
                │
                └─► ON ERROR (Invalid Code / Expired):
                          • Displays inline error: "Incorrect verification code. Please try again."
```

---

## 2. Airbnb-Style Dual-Role Architecture (`Landlord ↔ Tenant`)

### Why Dual-Role Switching?
In Nepal, many property owners (`Landlords`) also rent shops, offices, or apartments (`Tenants`). Forcing a user to register two separate accounts or locking their phone (`+977 98XXXXXXXX`) to only one role causes immense user friction.

### How Role Resolution Works:
1. **At Login (`PhoneOtpLoginScreen`)**:
   * The tab the user selects (`[ Landlord ]` vs `[ Tenant ]`) determines their initial requested workspace mode (`selectedRole`).
2. **On Verification (`verifyPhoneOtp`)**:
   * The user metadata in Supabase Auth JWT holds `roles` (list of enabled roles, e.g. `["LANDLORD", "TENANT"]`) and `current_active_role` (e.g. `"LANDLORD"`).
   * When `012345` is verified, `current_active_role` is set to `selectedRole`.
3. **In-App Mode Switching (Profile Settings)**:
   * A user currently inside the **Landlord Dashboard** can open their Profile Settings at any time and tap:
     * **`🔄 Switch to Tenant Dashboard`**
   * This instantly updates `current_active_role = "TENANT"` in state and routes them directly to their rented property view without requiring a re-login.

---

## 3. Navigation Routes (`Routes.kt`)

```kotlin
@Serializable
sealed interface Route : NavKey

/** Startup validation screen */
@Serializable
data object SplashRoute : Route

/** Step 1: Role selector + 10-digit Nepal phone number input (`+977`) */
@Serializable
data object PhoneOtpLoginRoute : Route

/** Step 2: 6-digit OTP verification code entry */
@Serializable
data class VerifyOtpRoute(
    val phoneNumber: String, // Clean format: "+9779841234567"
    val selectedRole: UserRole
) : Route

/** Main Landlord Dashboard */
@Serializable
data object PropertyListRoute : Route
```

---

## 4. State Hoisting & Previews Pattern (`Screen / Content / Preview`)

To maintain our design philosophy (`"Design for daily productivity first. Design for beauty second."`) and strict Compose Multiplatform requirements:
1. **`PhoneOtpLoginScreen` (Stateful Container)**:
   * Owns `AuthViewModel = koinViewModel()`, collects Orbit MVI side effects (`onNavigateToVerifyOtp`), and delegates rendering to `PhoneOtpLoginContent`.
2. **`PhoneOtpLoginContent` (Stateless UI)**:
   * Pure `@Composable` accepting current `selectedRole`, `phoneNumberInput`, `isLoading`, and action callbacks (`onRoleSelected`, `onPhoneChanged`, `onSendOtpClicked`).
3. **`VerifyOtpScreen` (Stateful Container)**:
   * Owns `AuthViewModel = koinViewModel()`, collects `onNavigateToHome`, and delegates rendering to `VerifyOtpContent`.
4. **`VerifyOtpContent` (Stateless UI)**:
   * Pure `@Composable` accepting `otpCodeInput`, `resendTimerSeconds`, `isLoading`, `errorMessage`, and action callbacks (`onOtpChanged`, `onVerifyClicked`, `onResendClicked`).
5. **Previews (`@Preview`)**:
   * Includes multi-state previews (`PhoneOtpLoginContentDefaultPreview`, `VerifyOtpContentDefaultPreview`) with rich Nepali-themed aesthetics and glassmorphic cards.
