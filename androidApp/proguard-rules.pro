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
-keep class io.ktor.** { *; }
-keep class io.ktor.client.** { *; }
-dontwarn io.ktor.**

# ──────────────────────────────────────────────────────────────────────────────
# Supabase
# ──────────────────────────────────────────────────────────────────────────────
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# ──────────────────────────────────────────────────────────────────────────────
# SQLDelight
# ──────────────────────────────────────────────────────────────────────────────
-keep class app.cash.sqldelight.** { *; }
-dontwarn app.cash.sqldelight.**

# ──────────────────────────────────────────────────────────────────────────────
# Koin
# ──────────────────────────────────────────────────────────────────────────────
-keep class org.koin.** { *; }
-keepnames class * extends org.koin.core.module.Module
-dontwarn org.koin.**

# ──────────────────────────────────────────────────────────────────────────────
# Orbit MVI
# ──────────────────────────────────────────────────────────────────────────────
-keep class org.orbitmvi.** { *; }
-dontwarn org.orbitmvi.**

# ──────────────────────────────────────────────────────────────────────────────
# Store5
# ──────────────────────────────────────────────────────────────────────────────
-keep class org.mobilenativefoundation.store.** { *; }
-dontwarn org.mobilenativefoundation.**

# ──────────────────────────────────────────────────────────────────────────────
# Coil
# ──────────────────────────────────────────────────────────────────────────────
-keep class coil3.** { *; }
-dontwarn coil3.**

# ──────────────────────────────────────────────────────────────────────────────
# Compose
# ──────────────────────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
