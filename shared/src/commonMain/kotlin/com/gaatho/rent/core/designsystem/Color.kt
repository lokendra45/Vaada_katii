package com.gaatho.rent.core.designsystem

import androidx.compose.ui.graphics.Color

// User Specified Palette
val LightBackground = Color(0xFFFAFAFA)
val DarkBackground = Color(0xFF0B0F0E)

val LightSurface = Color(0xFFFFFFFF)
val DarkSurface = Color(0xFF111514)

val LightInk = Color(0xFF111827)
val DarkInk = Color(0xFFF9FAFB) // Soft white

val LightSecondaryText = Color(0xFF4B5563)
val DarkSecondaryText = Color(0xFF9CA3AF) // Muted

val LightTertiaryText = Color(0xFF6B7280)
val DarkTertiaryText = Color(0xFF6B7280) // Dimmer

val LightBorder = Color(0xFFE5E7EB)
val DarkBorder = Color(0xFF374151) // Dark hairline

val LightDivider = Color(0xFFF3F4F6)
val DarkDivider = Color(0xFF1F2937) // Subtle

val LightPrimaryAccent = Color(0xFF0D7A5F) // Emerald
val DarkPrimaryAccent = Color(0xFF10B981) // Brighter Emerald

val LightDanger = Color(0xFFB42318) // Softer Red
val DarkDanger = Color(0xFFF87171)

// Material Theme Mappings - LIGHT
val PrimaryLight = LightPrimaryAccent
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = LightPrimaryAccent.copy(alpha = 0.15f)
val OnPrimaryContainerLight = LightPrimaryAccent

val SecondaryLight = LightPrimaryAccent
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = LightPrimaryAccent.copy(alpha = 0.15f)
val OnSecondaryContainerLight = LightPrimaryAccent

val TertiaryLight = LightSecondaryText
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = LightSecondaryText.copy(alpha = 0.15f)
val OnTertiaryContainerLight = LightSecondaryText

val BackgroundLight = LightBackground
val OnBackgroundLight = LightInk

val SurfaceLight = LightSurface
val OnSurfaceLight = LightInk

val SurfaceVariantLight = LightDivider
val OnSurfaceVariantLight = LightSecondaryText

val ErrorLight = LightDanger
val OnErrorLight = Color(0xFFFFFFFF)

val OutlineLight = LightTertiaryText
val OutlineVariantLight = LightBorder

// Material Theme Mappings - DARK
val PrimaryDark = DarkPrimaryAccent
val OnPrimaryDark = Color(0xFF000000)
val PrimaryContainerDark = DarkPrimaryAccent.copy(alpha = 0.2f)
val OnPrimaryContainerDark = DarkPrimaryAccent

val SecondaryDark = DarkPrimaryAccent
val OnSecondaryDark = Color(0xFF000000)
val SecondaryContainerDark = DarkPrimaryAccent.copy(alpha = 0.2f)
val OnSecondaryContainerDark = DarkPrimaryAccent

val TertiaryDark = DarkSecondaryText
val OnTertiaryDark = Color(0xFF000000)
val TertiaryContainerDark = DarkSecondaryText.copy(alpha = 0.2f)
val OnTertiaryContainerDark = DarkSecondaryText

val BackgroundDark = DarkBackground
val OnBackgroundDark = DarkInk

val SurfaceDark = DarkSurface
val OnSurfaceDark = DarkInk

val SurfaceVariantDark = DarkDivider
val OnSurfaceVariantDark = DarkSecondaryText

val ErrorDark = DarkDanger
val OnErrorDark = Color(0xFF000000)

val OutlineDark = DarkTertiaryText
val OutlineVariantDark = DarkBorder

/**
 * Rent Manager Nepal Design System Core Color Tokens (Direct Access)
 */
object AppColors {
    val TextPrimary = LightInk
    val TextSecondary = LightSecondaryText
    val TextTertiary = LightTertiaryText
    val TextDisabled = Color(0xFF9CA3AF)

    val Background = LightBackground
    val Surface = LightSurface
    val SurfaceVariant = LightDivider

    val Border = LightBorder
    val Divider = LightDivider

    val Success = Color(0xFF16A34A)
    val Warning = Color(0xFFD97706)
    val Error = LightDanger
    val Info = Color(0xFF2563EB)
}

