package com.gaatho.rent.core.designsystem

import androidx.compose.ui.graphics.Color

// Gaatho Unified Cool Tech Slate & Indigo Palette
val TextPrimary = Color(0xFF0A2540)
val TextSecondary = Color(0xFF425466)
val TextMuted = Color(0xFF697386)
val RoseAccent = Color(0xFFF43F5E)
val ErrorRed = Color(0xFFCF222E)
val InfoBlueTint = Color(0xFFDDF4FF)

val Indigo50 = Color(0xFFEEF2FF)
val Indigo100 = Color(0xFFE0E7FF)
val Indigo400 = Color(0xFF818CF8)
val Indigo600 = Color(0xFF4F46E5)
val Indigo700 = Color(0xFF4338CA)

val Emerald400 = Color(0xFF34D399)
val Emerald600 = Color(0xFF059669)
val Emerald700 = Color(0xFF047857)
val White = Color(0xFFFFFFFF)

val RichOnyx = TextPrimary
val SystemGray = TextMuted
val MetadataGray = TextMuted
val Slate900 = TextPrimary
val Slate800 = Color(0xFF1E3A5F)
val Slate700 = Color(0xFF334155)
val Slate600 = TextSecondary
val Slate500 = Color(0xFF64748B)
val Slate400 = TextMuted
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)
val Red = RoseAccent

// Mapping for RentManagerTheme Light & Dark color schemes
val PrimaryLight = Indigo600
val OnPrimaryLight = White
val PrimaryContainerLight = Indigo50
val OnPrimaryContainerLight = Indigo700

val PrimaryDark = Indigo400
val OnPrimaryDark = Slate900
val PrimaryContainerDark = Indigo600.copy(alpha = 0.2f)
val OnPrimaryContainerDark = Indigo100

val SecondaryLight = Emerald600
val OnSecondaryLight = White
val SecondaryContainerLight = Emerald400.copy(alpha = 0.15f)
val OnSecondaryContainerLight = Emerald700

val SecondaryDark = Emerald400
val OnSecondaryDark = Slate900
val SecondaryContainerDark = Emerald600.copy(alpha = 0.25f)
val OnSecondaryContainerDark = Emerald400

val TertiaryLight = RoseAccent
val OnTertiaryLight = White
val TertiaryContainerLight = RoseAccent.copy(alpha = 0.15f)
val OnTertiaryContainerLight = Color(0xFF881337)

val TertiaryDark = Color(0xFFFB7185)
val OnTertiaryDark = Slate900
val TertiaryContainerDark = RoseAccent.copy(alpha = 0.25f)
val OnTertiaryContainerDark = Color(0xFFFB7185)

val SurfaceLight = White
val OnSurfaceLight = Slate800
val SurfaceVariantLight = Slate100
val OnSurfaceVariantLight = Slate500
val BackgroundLight = Slate50
val OnBackgroundLight = Slate800

val SurfaceDark = Slate800
val OnSurfaceDark = Slate50
val SurfaceVariantDark = Slate700
val OnSurfaceVariantDark = Slate300
val BackgroundDark = Slate900
val OnBackgroundDark = Slate50

val ErrorLight = ErrorRed
val OnErrorLight = White
val ErrorDark = Color(0xFFF87171)
val OnErrorDark = Slate900

/**
 * Rent Manager Nepal Design System v1.0 Core Color Tokens.
 */
object AppColors {
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF4B5563)
    val TextTertiary = Color(0xFF6B7280)
    val TextDisabled = Color(0xFF9CA3AF)

    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF8FAFC)

    val Border = Color(0xFFE5E7EB)
    val Divider = Color(0xFFF3F4F6)

    val Success = Color(0xFF16A34A)
    val Warning = Color(0xFFD97706)
    val Error = Color(0xFFDC2626)
    val Info = Color(0xFF2563EB)
}

