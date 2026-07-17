package com.gaatho.rent.core.designsystem

import androidx.compose.ui.graphics.Color

// ==========================================
// LIGHT THEME COLORS
// ==========================================
val EmeraldLightPrimary = Color(0xFF0d7a5f)
val EmeraldLightOnPrimary = Color(0xFFffffff)
val EmeraldLightPrimaryContainer = Color(0xFF0d7a5f)
val EmeraldLightOnPrimaryContainer = Color(0xFFe6f3ed)
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
val EmeraldLightOnBackground = Color(0xFF1a1c1e)

val EmeraldLightSurface = Color(0xFFf9f9f9)
val EmeraldLightOnSurface = Color(0xFF1a1c1e)
val EmeraldLightSurfaceVariant = Color(0xFFf3f3f3) // Unfocused Background
val EmeraldLightOnSurfaceVariant = Color(0xFF44474a) // Labels

val EmeraldLightInverseSurface = Color(0xFF2f3131)
val EmeraldLightInverseOnSurface = Color(0xFFf0f1f1)

val EmeraldLightOutline = Color(0xFF74777f) // Placeholders
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

/**
 * Extended semantic colors for specific UI elements like status badges and avatars.
 * Values are stored as Longs so ViewModels can map to them without depending on Compose Color.
 */
object ExtendedColorHex {
    // Property Status Badges
    const val VacantBackground = 0xFFFFEBEE
    const val VacantText = 0xFFC62828
    const val VacantBorder = 0xFFFFCDD2

    const val OccupiedBackground = 0xFFE8F5E9
    const val OccupiedText = 0xFF2E7D32
    const val OccupiedBorder = 0xFFC8E6C9

    // Tenant Status Badges
    const val ActiveBackground = 0xFFD8F3E5
    const val ActiveText = 0xFF0F6E4A
    
    const val InactiveBackground = 0xFFF1F3F5
    const val InactiveText = 0xFF495057

    // Avatar Color Pairs (Background to Text)
    val AvatarPairs = listOf(
        Pair(0xFFD8F3E5, 0xFF0D684D), // Teal / Mint
        Pair(0xFFE0F2E9, 0xFF1B6342), // Soft Green
        Pair(0xFFDAF0F2, 0xFF115C61), // Soft Cyan
        Pair(0xFFE4E8F0, 0xFF2C3E50), // Slate
        Pair(0xFFE8F0FE, 0xFF1A73E8), // Soft Blue
        Pair(0xFFF3E8FF, 0xFF6B21A8)  // Soft Lavender
    )
}
