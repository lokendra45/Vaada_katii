@file:JvmName("DesignSystemColors")
package com.gaatho.rent.core.designsystem

import kotlin.jvm.JvmName
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
// LIGHT THEME (Figma Spec Neutrals)
// ══════════════════════════════════════════════════════════════════════════════

// Primary — Emerald Green
val LightPrimary              = Color(0xFF059669)
val LightOnPrimary            = Color(0xFFFFFFFF)
val LightPrimaryContainer     = Color(0xFFD1FAE5)
val LightOnPrimaryContainer   = Color(0xFF064E3B)
val LightInversePrimary       = Color(0xFF6EE7B7)
val LightPrimaryFixed         = Color(0xFFD1FAE5)
val LightPrimaryFixedDim      = Color(0xFF10B981)
val LightOnPrimaryFixed       = Color(0xFF064E3B)
val LightOnPrimaryFixedVariant= Color(0xFF0D7554)

// Figma Neutrals
val LightSurface                = Color(0xFFFFFFFF) // White cards
val LightSurfaceDim             = Color(0xFFF9FAFB) // Gray-50
val LightSurfaceBright          = Color(0xFFFFFFFF)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF) // Base white (Cards)
val LightSurfaceContainerLow    = Color(0xFFF9FAFB) // Gray-50
val LightSurfaceContainer       = Color(0xFFF3F4F6) // Gray-100
val LightSurfaceContainerHigh   = Color(0xFFE5E7EB) // Gray-200
val LightSurfaceContainerHighest= Color(0xFFD1D5DB) // Gray-300
val LightSurfaceVariant         = Color(0xFFF3F4F6) // Gray-100
val LightSurfaceTint            = Color(0xFF059669)

val LightOnSurface        = Color(0xFF1E293B) // Slate-800
val LightOnSurfaceVariant = Color(0xFF64748B) // Slate-500
val LightInverseSurface   = Color(0xFF1E293B) // Slate-800
val LightInverseOnSurface = Color(0xFFF9FAFB) // Gray-50

val LightBackground   = Color(0xFFF9FAFB) // Gray-50
val LightOnBackground = Color(0xFF1E293B) // Slate-800

val LightOutline        = Color(0xFFD1D5DB) // Gray-300
val LightOutlineVariant = Color(0xFFE5E7EB) // Gray-200

// Secondary
val Secondary              = Color(0xFF575E70)
val OnSecondary            = Color(0xFFFFFFFF)
val SecondaryContainer     = Color(0xFFD9DFF5)
val OnSecondaryContainer   = Color(0xFF5C6274)
val SecondaryFixed         = Color(0xFFDCE2F7)
val SecondaryFixedDim      = Color(0xFFC0C6DB)
val OnSecondaryFixed       = Color(0xFF141B2B)
val OnSecondaryFixedVariant= Color(0xFF404758)

// Tertiary
val Tertiary              = Color(0xFF7E3000)
val OnTertiary            = Color(0xFFFFFFFF)
val TertiaryContainer     = Color(0xFFA44100)
val OnTertiaryContainer   = Color(0xFFFFD2BE)
val TertiaryFixed         = Color(0xFFFFDBCC)
val TertiaryFixedDim      = Color(0xFFFFB695)
val OnTertiaryFixed       = Color(0xFF351000)
val OnTertiaryFixedVariant= Color(0xFF7B2F00)

// Error
val Error              = Color(0xFFDB354F)
val OnError            = Color(0xFFFFFFFF)
val ErrorContainer     = Color(0xFFFEE2E2)
val OnErrorContainer   = Color(0xFFDB354F)

val SurfaceContainerLowest  = Color(0xFFFFFFFF) 
val SurfaceContainerLow     = Color(0xFFF8FAFC) // Slate 50
val SurfaceContainer        = Color(0xFFF1F5F9) // Slate 100
val SurfaceContainerHigh    = Color(0xFFE2E8F0) // Slate 200
val SurfaceContainerHighest = Color(0xFFCBD5E1) // Slate 300
val SurfaceTint             = Color(0xFF00A86B)
val SurfaceVariant          = Color(0xFFF1F5F9)

// On-surface
val OnSurface        = Color(0xFF0F172A) // Deep Slate
val OnSurfaceVariant = Color(0xFF64748B) // Slate 500 (softer secondary text)
val InverseSurface   = Color(0xFF0F172A)
val InverseOnSurface = Color(0xFFFFFFFF)

// Background
val Background   = Color(0xFFFFFFFF)
val OnBackground = Color(0xFF0F172A)

// Outline (Fainter, since we use shadows instead)
val Outline        = Color(0xFFE2E8F0) // Slate 200
val OutlineVariant = Color(0xFFF1F5F9) // Slate 100

// ══════════════════════════════════════════════════════════════════════════════
// EMERALD — DARK THEME (Deep Charcoal + Emerald Green Accent)
// ══════════════════════════════════════════════════════════════════════════════

// Primary — Emerald stays vibrant in dark mode
val DarkPrimary              = Color(0xFF4ADE80) // Bright emerald for dark surfaces
val DarkOnPrimary            = Color(0xFF003822)
val DarkPrimaryContainer     = Color(0xFF1A3D2E) // Deep green container
val DarkOnPrimaryContainer   = Color(0xFFB8F5D3)
val DarkInversePrimary       = Color(0xFF00A86B)
val DarkPrimaryFixed         = Color(0xFFB8F5D3)
val DarkPrimaryFixedDim      = Color(0xFF4ADE80)
val DarkOnPrimaryFixed       = Color(0xFF002114)
val DarkOnPrimaryFixedVariant= Color(0xFF00804F)

val DarkSecondary              = Color(0xFFA8B5C8)
val DarkOnSecondary            = Color(0xFF1C2733)
val DarkSecondaryContainer     = Color(0xFF2A3544)
val DarkOnSecondaryContainer   = Color(0xFFBCC8D9)
val DarkSecondaryFixed         = Color(0xFFD5E3FD)
val DarkSecondaryFixedDim      = Color(0xFFA8B5C8)
val DarkOnSecondaryFixed       = Color(0xFF0D1C2F)
val DarkOnSecondaryFixedVariant= Color(0xFF3A485C)

val DarkTertiary              = Color(0xFFF7BD3E)
val DarkOnTertiary            = Color(0xFF402D00)
val DarkTertiaryContainer     = Color(0xFF3D3520)
val DarkOnTertiaryContainer   = Color(0xFFFFDEA3)
val DarkTertiaryFixed         = Color(0xFFFFDEA3)
val DarkTertiaryFixedDim      = Color(0xFFF7BD3E)
val DarkOnTertiaryFixed       = Color(0xFF261900)
val DarkOnTertiaryFixedVariant= Color(0xFF5D4200)

val DarkError              = Color(0xFFFFB4AB)
val DarkOnError            = Color(0xFF690005)
val DarkErrorContainer     = Color(0xFF3D1518)
val DarkOnErrorContainer   = Color(0xFFFFDAD6)

// Surface scale — Dark mode mapped to Figma charcoal neutrals
val DarkSurface                = Color(0xFF111729) // Dark Navy
val DarkSurfaceDim             = Color(0xFF111729)
val DarkSurfaceBright          = Color(0xFF111827)
val DarkSurfaceContainerLowest = Color(0xFF111729) 
val DarkSurfaceContainerLow    = Color(0xFF111827) // Gray-900
val DarkSurfaceContainer       = Color(0xFF160042) // Dark Purple
val DarkSurfaceContainerHigh   = Color(0xFF374151) // Gray-600
val DarkSurfaceContainerHighest= Color(0xFF6B7280) // Gray-500
val DarkSurfaceVariant         = Color(0xFF374151) // Gray-600
val DarkSurfaceTint            = Color(0xFF4ADE80)

val DarkOnSurface        = Color(0xFFF9FAFB) // Gray-50
val DarkOnSurfaceVariant = Color(0xFF9CA3AF) // Gray-400
val DarkInverseSurface   = Color(0xFFF9FAFB)
val DarkInverseOnSurface = Color(0xFF111729)

val DarkBackground   = Color(0xFF111729) // Dark Navy
val DarkOnBackground = Color(0xFFF3F4F6) // Gray-100

val DarkOutline        = Color(0xFF374151) // Gray-600
val DarkOutlineVariant = Color(0xFF111827) // Gray-900

// ══════════════════════════════════════════════════════════════════════════════
// SEMANTIC TOKENS & GRADIENT COLORS — Safe to use from Compose UI
// ══════════════════════════════════════════════════════════════════════════════

object AppColors {
    val DashboardBackground = Color(0xFFF9FAFB)
    val CardBorder = Color(0xFFE5E7EB)
    val ProgressTrack = Color(0xFFE5E7EB)
    val TextPrimary = Color(0xFF1E293B)
    val TextMuted = Color(0xFF64748B)
    val ShadowAmbient = Color(0x14000000)
    val ShadowSpot = Color(0x05000000)
    val HeroGlow = Color(0x1AD2FF1F)

    // Emerald Accent
    val EmeraldAccent = Color(0xFF059669)
    val EmeraldAccentDeep = Color(0xFF065F46)
    val EmeraldAccentDark = Color(0xFF0D7554)
    val EmeraldAccentLight = Color(0xFFECFDF5)
    val EmeraldAccentBorder = Color(0xFFA7F3D0)
    /** Success — green 500 */
    val Success           = Color(0xFF059669)
    val SuccessContainer  = Color(0xFFECFDF5)
    val OnSuccess         = Color(0xFF0D7554)

    /** Warning — amber 500 */
    val Warning           = Color(0xFFF59E0B)
    val WarningContainer  = Color(0xFFFFFBEB)
    val OnWarning         = Color(0xFF78350F)

    /** Error — matches Material error token */
    val Error             = Color(0xFFDB354F)
    val ErrorContainer    = Color(0xFFFEE2E2)
    val OnError           = Color(0xFFFFFFFF)

    val AvatarWarm = Color(0xFF7C2D12)
    val AvatarNeutral = Color(0xFFF1F5F9)
    val AvatarSuccess = Color(0xFFD1FAE5)
    val AvatarError = Color(0xFFFEE2E2)

    /** Info — same as primary accent */
    val Info              = Color(0xFF3525CD)
    val InfoContainer     = Color(0xFFE2DFFF)
    val OnInfo            = Color(0xFFFFFFFF)
}

object ExtendedColorHex {
    const val VacantBackground = 0xFFFFDAD6L
    const val VacantText = 0xFFBA1A1AL
    const val VacantBorder = 0xFFFFB4ABL

    const val OccupiedBackground = 0xFFF0FDF4L
    const val OccupiedText = 0xFF22C55EL
    const val OccupiedBorder = 0xFFBBF7D0L

    const val ActiveBackground = 0xFFF0FDF4L
    const val ActiveText = 0xFF16A34AL

    const val InactiveBackground = 0xFFF3F4F6L
    const val InactiveText = 0xFF6B7280L

    val AvatarPairs = listOf(
        Pair(0xFFE2DFFFL, 0xFF3323CCL),
        Pair(0xFFF0FDF4L, 0xFF15803DL),
        Pair(0xFFE0F2FEL, 0xFF0369A1L),
        Pair(0xFFFDF4FFL, 0xFF7E22CEL),
        Pair(0xFFFFD2BEL, 0xFF7E3000L),
        Pair(0xFFDCE2F7L, 0xFF404758L)
    )
}

