# ProGuard / R8 rules for RentManagerApp
# Keep this file up to date as new dependencies are added.

# ──────────────────────────────────────────────────────────────────────────────
# Kotlin
# ──────────────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ──────────────────────────────────────────────────────────────────────────────
# Kotlin Serialization
# Critical: @SerialName annotations on UiState sealed interface variants protect
# against R8 renaming, but we still need to keep the serializer companions.
# ──────────────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep all @Serializable classes in this app
-keep,includedescriptorclasses class com.gaatho.rent.**$$serializer { *; }
-keepclassmembers class com.gaatho.rent.** {
    *** Companion;
}
-keepclasseswithmembers class com.gaatho.rent.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep sealed interface serialization descriptors
-keep class com.gaatho.rent.core.ui.UiState { *; }
-keep class com.gaatho.rent.core.ui.UiState$* { *; }
-keep class com.gaatho.rent.features.**.presentation.**State { *; }
-keep class com.gaatho.rent.features.**.domain.model.** { *; }

# ──────────────────────────────────────────────────────────────────────────────
# Ktor Client
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn io.ktor.**

# ──────────────────────────────────────────────────────────────────────────────
# Supabase
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn io.github.jan.supabase.**

# ──────────────────────────────────────────────────────────────────────────────
# SQLDelight
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn app.cash.sqldelight.**

# ──────────────────────────────────────────────────────────────────────────────
# Koin
# ──────────────────────────────────────────────────────────────────────────────
-keepnames class * extends org.koin.core.module.Module
-dontwarn org.koin.**

# ──────────────────────────────────────────────────────────────────────────────
# Orbit MVI
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn org.orbitmvi.**

# ──────────────────────────────────────────────────────────────────────────────
# Store5
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn org.mobilenativefoundation.**

# ──────────────────────────────────────────────────────────────────────────────
# Coil
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn coil3.**

# ──────────────────────────────────────────────────────────────────────────────
# Compose
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn androidx.compose.**

# ──────────────────────────────────────────────────────────────────────────────
# Sandwich (Network Responses)
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn com.skydoves.sandwich.**

# ──────────────────────────────────────────────────────────────────────────────
# FileKit
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn io.github.vinceglb.filekit.**

# ──────────────────────────────────────────────────────────────────────────────
# Firebase & Play Services
# ──────────────────────────────────────────────────────────────────────────────
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
