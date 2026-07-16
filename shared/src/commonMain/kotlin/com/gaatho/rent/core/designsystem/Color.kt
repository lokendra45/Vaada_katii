package com.gaatho.rent.core.designsystem

import androidx.compose.ui.graphics.Color

// ==========================================
// LIGHT THEME COLORS
// ==========================================
val EmeraldLightPrimary = Color(0xFF005f49)
val EmeraldLightOnPrimary = Color(0xFFffffff)
val EmeraldLightPrimaryContainer = Color(0xFF0d7a5f)
val EmeraldLightOnPrimaryContainer = Color(0xFFa8ffdf)
val EmeraldLightInversePrimary = Color(0xFF7cd8b8)

val EmeraldLightSecondary = Color(0xFF575e70)
val EmeraldLightOnSecondary = Color(0xFFffffff)
val EmeraldLightSecondaryContainer = Color(0xFFd9dff5)
val EmeraldLightOnSecondaryContainer = Color(0xFF5c6274)

val EmeraldLightTertiary = Color(0xFF4a5462)
val EmeraldLightOnTertiary = Color(0xFFffffff)
val EmeraldLightTertiaryContainer = Color(0xFF626c7b)
val EmeraldLightOnTertiaryContainer = Color(0xFFe4eeff)

val EmeraldLightError = Color(0xFFba1a1a)
val EmeraldLightOnError = Color(0xFFffffff)
val EmeraldLightErrorContainer = Color(0xFFffdad6)
val EmeraldLightOnErrorContainer = Color(0xFF93000a)

val EmeraldLightBackground = Color(0xFFf9f9f9)
val EmeraldLightOnBackground = Color(0xFF1a1c1c)

val EmeraldLightSurface = Color(0xFFf9f9f9)
val EmeraldLightOnSurface = Color(0xFF1a1c1c)
val EmeraldLightSurfaceVariant = Color(0xFFe2e2e2)
val EmeraldLightOnSurfaceVariant = Color(0xFF3e4944)

val EmeraldLightInverseSurface = Color(0xFF2f3131)
val EmeraldLightInverseOnSurface = Color(0xFFf0f1f1)

val EmeraldLightOutline = Color(0xFF6e7a74)
val EmeraldLightOutlineVariant = Color(0xFFbdc9c2)

// ==========================================
// DARK THEME COLORS
// ==========================================
val EmeraldDarkPrimary = Color(0xFF7cd8b8)
val EmeraldDarkOnPrimary = Color(0xFF00382a)
val EmeraldDarkPrimaryContainer = Color(0xFF0d7a5f)
val EmeraldDarkOnPrimaryContainer = Color(0xFFa8ffdf)
val EmeraldDarkInversePrimary = Color(0xFF006c53)

val EmeraldDarkSecondary = Color(0xFFc0c6db)
val EmeraldDarkOnSecondary = Color(0xFF293040)
val EmeraldDarkSecondaryContainer = Color(0xFF404758)
val EmeraldDarkOnSecondaryContainer = Color(0xFFaeb5c9)

val EmeraldDarkTertiary = Color(0xFFbdc7d8)
val EmeraldDarkOnTertiary = Color(0xFF27313e)
val EmeraldDarkTertiaryContainer = Color(0xFF626c7b)
val EmeraldDarkOnTertiaryContainer = Color(0xFFe4eeff)

val EmeraldDarkError = Color(0xFFffb4ab)
val EmeraldDarkOnError = Color(0xFF690005)
val EmeraldDarkErrorContainer = Color(0xFF93000a)
val EmeraldDarkOnErrorContainer = Color(0xFFffdad6)

val EmeraldDarkBackground = Color(0xFF121414)
val EmeraldDarkOnBackground = Color(0xFFe2e2e2)

val EmeraldDarkSurface = Color(0xFF121414)
val EmeraldDarkOnSurface = Color(0xFFe2e2e2)
val EmeraldDarkSurfaceVariant = Color(0xFF333535)
val EmeraldDarkOnSurfaceVariant = Color(0xFFbdc9c2)

val EmeraldDarkInverseSurface = Color(0xFFe2e2e2)
val EmeraldDarkInverseOnSurface = Color(0xFF2f3131)

val EmeraldDarkOutline = Color(0xFF88938d)
val EmeraldDarkOutlineVariant = Color(0xFF3e4944)

/**
 * Rent Manager Nepal Design System Core Color Tokens (Direct Access)
 * Using standard hardcoded names that map dynamically based on standard colors.
 * Note: AppColors is kept for compatibility, but material theme colorScheme is preferred.
 */
object AppColors {
    val Success = Color(0xFF005f49) // Using primary for success
    val Warning = Color(0xFFD97706) // Keep as fallback
    val Error = EmeraldLightError
    val Info = EmeraldLightSecondary
}
