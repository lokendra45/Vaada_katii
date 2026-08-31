@file:JvmName("DesignSystemColors")
package com.gaatho.rent.core.designsystem

import kotlin.jvm.JvmName
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
// BRAND PALETTE — Figma Design System
// ══════════════════════════════════════════════════════════════════════════════

// ─── Light Theme ─────────────────────────────────────────────────────────────

val LightPrimary            = Color(0xFF006FFD) // Highlight main
val LightOnPrimary          = Color(0xFFFFFFFF)
val LightPrimaryContainer   = Color(0xFFEAF2FF) 
val LightOnPrimaryContainer = Color(0xFF006FFD) 
val LightInversePrimary     = Color(0xFF6FBAFF) 

val LightBackground         = Color(0xFFF8F9FE) 
val LightOnBackground       = Color(0xFF1F2024)

val LightSurface            = Color(0xFFFFFFFF) 
val LightOnSurface          = Color(0xFF1F2024)
val LightOnSurfaceVariant   = Color(0xFF71727A) 
val LightSurfaceVariant     = Color(0xFFE8E9F1)
val LightSurfaceTint        = Color(0xFF006FFD)
val LightInverseSurface     = Color(0xFF1F2024)
val LightInverseOnSurface   = Color(0xFFF8F9FE)

val LightSurfaceContainerLowest  = Color(0xFFFFFFFF)
val LightSurfaceContainerLow     = Color(0xFFF8F9FE)
val LightSurfaceContainer        = Color(0xFFE8E9F1)
val LightSurfaceContainerHigh    = Color(0xFFD4D6DD)
val LightSurfaceContainerHighest = Color(0xFFC5C6CC)

val LightOutline        = Color(0xFFC5C6CC) 
val LightOutlineVariant = Color(0xFFE8E9F1) 

val LightSecondary            = Color(0xFF71727A)
val LightOnSecondary          = Color(0xFFFFFFFF)
val LightSecondaryContainer   = Color(0xFFE8E9F1)
val LightOnSecondaryContainer = Color(0xFF2F3036)

val LightTertiary            = Color(0xFFE86339)
val LightOnTertiary          = Color(0xFFFFFFFF)
val LightTertiaryContainer   = Color(0xFFFFF4E4)
val LightOnTertiaryContainer = Color(0xFFE86339)

val LightError            = Color(0xFFED3241)
val LightOnError          = Color(0xFFFFFFFF)
val LightErrorContainer   = Color(0xFFFFE2E5)
val LightOnErrorContainer = Color(0xFFED3241)

// ─── Dark Theme ──────────────────────────────────────────────────────────────

val DarkPrimary            = Color(0xFF006FFD) 
val DarkOnPrimary          = Color(0xFFFFFFFF)
val DarkPrimaryContainer   = Color(0xFF2897FF) 
val DarkOnPrimaryContainer = Color(0xFFEAF2FF) 
val DarkInversePrimary     = Color(0xFF006FFD)

val DarkBackground         = Color(0xFF1F2024) 
val DarkOnBackground       = Color(0xFFFFFFFF) 

val DarkSurface            = Color(0xFF2F3036) 
val DarkOnSurface          = Color(0xFFFFFFFF) 
val DarkOnSurfaceVariant   = Color(0xFF8F9098) 
val DarkSurfaceVariant     = Color(0xFF494A50)
val DarkSurfaceTint        = Color(0xFF006FFD)
val DarkInverseSurface     = Color(0xFFFFFFFF)
val DarkInverseOnSurface   = Color(0xFF1F2024)

val DarkSurfaceContainerLowest  = Color(0xFF1F2024) 
val DarkSurfaceContainerLow     = Color(0xFF2F3036) 
val DarkSurfaceContainer        = Color(0xFF494A50) 
val DarkSurfaceContainerHigh    = Color(0xFF71727A) 
val DarkSurfaceContainerHighest = Color(0xFF8F9098) 

val DarkOutline        = Color(0xFF71727A) 
val DarkOutlineVariant = Color(0xFF494A50) 

val DarkSecondary            = Color(0xFF8F9098)
val DarkOnSecondary          = Color(0xFF1F2024)
val DarkSecondaryContainer   = Color(0xFF494A50)
val DarkOnSecondaryContainer = Color(0xFFE8E9F1)

val DarkTertiary            = Color(0xFFFFB37C)
val DarkOnTertiary          = Color(0xFF1F2024)
val DarkTertiaryContainer   = Color(0xFFE86339)
val DarkOnTertiaryContainer = Color(0xFFFFF4E4)

val DarkError            = Color(0xFFFF616D)
val DarkOnError          = Color(0xFF1F2024)
val DarkErrorContainer   = Color(0xFFED3241)
val DarkOnErrorContainer = Color(0xFFFFE2E5)

// ══════════════════════════════════════════════════════════════════════════════
// SEMANTIC TOKENS — use these in Compose UI (never raw hex)
// ══════════════════════════════════════════════════════════════════════════════

object AppColors {
    // ── Text ─────────────────────────────────────────────────────────────────
    val TextPrimary = Color(0xFF1F2024)
    val TextMuted   = Color(0xFF71727A)

    // ── Surfaces ─────────────────────────────────────────────────────────────
    val CardBorder          = Color(0xFFC5C6CC)
    val ProgressTrack       = Color(0xFFE8E9F1)
    val DashboardBackground = Color(0xFFF8F9FE)
    val ShadowAmbient       = Color(0x14000000)
    val ShadowSpot          = Color(0x05000000)

    // ── Primary ─────────────────────────────────────────────────────────
    val EmeraldAccent       = Color(0xFF006FFD) 
    val EmeraldAccentDeep   = Color(0xFF2897FF) 
    val EmeraldAccentDark   = Color(0xFF006FFD) 
    val EmeraldAccentLight  = Color(0xFFEAF2FF) 
    val EmeraldAccentBorder = Color(0xFFB4DBFF) 
    val HeroGlow            = Color(0x1A006FFD)

    // ── Success ───────────────────────────────────────────────────
    val Success          = Color(0xFF298267)
    val SuccessContainer = Color(0xFFE7F4E8)
    val OnSuccess        = Color(0xFFFFFFFF)

    // ── Warning ──────────────────────────────────────────────────────────────
    val Warning          = Color(0xFFE86339)
    val WarningContainer = Color(0xFFFFF4E4)
    val OnWarning        = Color(0xFFFFFFFF)

    // ── Error ────────────────────────────────────────────────────────────────
    val Error          = Color(0xFFED3241)
    val ErrorContainer = Color(0xFFFFE2E5)
    val OnError        = Color(0xFFFFFFFF)

    // ── Avatar palette ───────────────────────────────────────────────────────
    val AvatarWarm    = Color(0xFFE86339)
    val AvatarNeutral = Color(0xFFE8E9F1)
    val AvatarSuccess = Color(0xFFE7F4E8)
    val AvatarError   = Color(0xFFFFE2E5)
}

// ── Extended hex constants (used in dynamic Color() constructors) ─────────────
object ExtendedColorHex {
    const val VacantBackground = 0xFFFFE2E5L
    const val VacantText       = 0xFFED3241L
    const val VacantBorder     = 0xFFFF616DL

    const val OccupiedBackground = 0xFFE7F4E8L
    const val OccupiedText       = 0xFF298267L
    const val OccupiedBorder     = 0xFF3AC0A0L

    const val ActiveBackground = 0xFFEAF2FFL 
    const val ActiveText       = 0xFF006FFDL 

    const val InactiveBackground = 0xFFE8E9F1L
    const val InactiveText       = 0xFF71727AL

    val AvatarPairs = listOf(
        Pair(0xFFEAF2FFL, 0xFF006FFDL),
        Pair(0xFFE7F4E8L, 0xFF298267L),
        Pair(0xFFB4DBFFL, 0xFF006FFDL),
        Pair(0xFFFFF4E4L, 0xFFE86339L),
        Pair(0xFFE8E9F1L, 0xFF1F2024L)
    )
}
