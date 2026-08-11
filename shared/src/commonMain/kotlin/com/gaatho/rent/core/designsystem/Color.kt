package com.gaatho.rent.core.designsystem

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
// QUIET PREMIUM — LIGHT THEME
// Source: design-tokens.yaml (exact values)
// ══════════════════════════════════════════════════════════════════════════════

// Primary (Emerald Green Accent)
val Primary              = Color(0xFF00A86B)
val OnPrimary            = Color(0xFFFFFFFF)
val PrimaryContainer     = Color(0xFFE5F9F4)
val OnPrimaryContainer   = Color(0xFF005234)
val InversePrimary       = Color(0xFFC3C0FF)
val PrimaryFixed         = Color(0xFFE2DFFF)
val PrimaryFixedDim      = Color(0xFFC3C0FF)
val OnPrimaryFixed       = Color(0xFF0F0069)
val OnPrimaryFixedVariant= Color(0xFF3323CC)

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
val Error              = Color(0xFFBA1A1A)
val OnError            = Color(0xFFFFFFFF)
val ErrorContainer     = Color(0xFFFFDAD6)
val OnErrorContainer   = Color(0xFF93000A)

// Surface scale - Premium Stark White
val Surface                 = Color(0xFFFFFFFF) // Pure White
val SurfaceDim              = Color(0xFFF1F5F9) // Slate 100
val SurfaceBright           = Color(0xFFFFFFFF)
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

// Surface scale — True deep charcoal (Finzo dark mode look)
val DarkSurface                = Color(0xFF0D0F0E) // Near-black with slight green tint
val DarkSurfaceDim             = Color(0xFF0D0F0E)
val DarkSurfaceBright          = Color(0xFF252927)
val DarkSurfaceContainerLowest = Color(0xFF070908) // Deepest black
val DarkSurfaceContainerLow    = Color(0xFF141816) // Card backgrounds
val DarkSurfaceContainer       = Color(0xFF1A1E1C) // Slightly elevated
val DarkSurfaceContainerHigh   = Color(0xFF232826) // Elevated containers
val DarkSurfaceContainerHighest= Color(0xFF2E3331) // Highest elevation
val DarkSurfaceVariant         = Color(0xFF1A1E1C)
val DarkSurfaceTint            = Color(0xFF4ADE80)

val DarkOnSurface        = Color(0xFFF0F0F0) // Crisp white text
val DarkOnSurfaceVariant = Color(0xFF8A9490) // Muted sage for secondary text
val DarkInverseSurface   = Color(0xFFF0F0F0)
val DarkInverseOnSurface = Color(0xFF0D0F0E)

val DarkBackground   = Color(0xFF070908) // True deep black
val DarkOnBackground = Color(0xFFF0F0F0)

val DarkOutline        = Color(0xFF3A403E) // Subtle borders
val DarkOutlineVariant = Color(0xFF1E2422) // Very faint borders

// ══════════════════════════════════════════════════════════════════════════════
// SEMANTIC TOKENS & GRADIENT COLORS — Safe to use from Compose UI
// ══════════════════════════════════════════════════════════════════════════════

object AppColors {
    // Emerald Accent
    val EmeraldAccent = Color(0xFF00A86B)
    val EmeraldAccentLight = Color(0xFFE5F9F4)
    /** Success — green 500 */
    val Success           = Color(0xFF22C55E)
    val SuccessContainer  = Color(0xFFF0FDF4)
    val OnSuccess         = Color(0xFF14532D)

    /** Warning — amber 500 */
    val Warning           = Color(0xFFF59E0B)
    val WarningContainer  = Color(0xFFFFFBEB)
    val OnWarning         = Color(0xFF78350F)

    /** Error — matches Material error token */
    val Error             = Color(0xFFBA1A1A)
    val ErrorContainer    = Color(0xFFFFDAD6)
    val OnError           = Color(0xFFFFFFFF)

    /** Info — same as primary accent */
    val Info              = Color(0xFF3525CD)
    val InfoContainer     = Color(0xFFE2DFFF)
    val OnInfo            = Color(0xFFFFFFFF)
}

// ══════════════════════════════════════════════════════════════════════════════
// EXTENDED SEMANTIC HEX — ViewModel-safe (Long constants, no Compose import)
// ══════════════════════════════════════════════════════════════════════════════

object ExtendedColorHex {
    // Property Status Badges
    const val VacantBackground = 0xFFFFDAD6L
    const val VacantText       = 0xFFBA1A1AL
    const val VacantBorder     = 0xFFFFB4ABL

    const val OccupiedBackground = 0xFFF0FDF4L
    const val OccupiedText       = 0xFF22C55EL
    const val OccupiedBorder     = 0xFFBBF7D0L

    // Tenant Status Badges
    const val ActiveBackground   = 0xFFF0FDF4L
    const val ActiveText         = 0xFF16A34AL

    const val InactiveBackground = 0xFFF3F4F6L
    const val InactiveText       = 0xFF6B7280L

    // Avatar pairs: (background, text) as Long
    val AvatarPairs = listOf(
        Pair(0xFFE2DFFFL, 0xFF3323CCL), // Indigo
        Pair(0xFFF0FDF4L, 0xFF15803DL), // Green
        Pair(0xFFE0F2FEL, 0xFF0369A1L), // Sky
        Pair(0xFFFDF4FFL, 0xFF7E22CEL), // Purple
        Pair(0xFFFFD2BEL, 0xFF7E3000L), // Terracotta (tertiary)
        Pair(0xFFDCE2F7L, 0xFF404758L)  // Slate (secondary)
    )
}
