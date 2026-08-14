@file:JvmName("DesignSystemColors")
package com.gaatho.rent.core.designsystem

import kotlin.jvm.JvmName
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
// BRAND PALETTE — #2563EB Royal Blue
// ══════════════════════════════════════════════════════════════════════════════

// ─── Light Theme ─────────────────────────────────────────────────────────────

val LightPrimary            = Color(0xFF2563EB) // Royal Blue — buttons, FAB, CTAs
val LightOnPrimary          = Color(0xFFFFFFFF)
val LightPrimaryContainer   = Color(0xFFDBEAFE) // blue-100
val LightOnPrimaryContainer = Color(0xFF1E3A8A) // blue-900
val LightInversePrimary     = Color(0xFF60A5FA) // blue-400

val LightBackground         = Color(0xFFF1F5F9) // cool gray — screen backgrounds
val LightOnBackground       = Color(0xFF111827)

val LightSurface            = Color(0xFFFFFFFF) // pure white — cards
val LightOnSurface          = Color(0xFF111827)
val LightOnSurfaceVariant   = Color(0xFF6B7280) // muted text
val LightSurfaceVariant     = Color(0xFFF3F4F6)
val LightSurfaceTint        = Color(0xFF2563EB)
val LightInverseSurface     = Color(0xFF111827)
val LightInverseOnSurface   = Color(0xFFF1F5F9)

val LightSurfaceContainerLowest  = Color(0xFFFFFFFF)
val LightSurfaceContainerLow     = Color(0xFFF1F5F9)
val LightSurfaceContainer        = Color(0xFFE2E8F0)
val LightSurfaceContainerHigh    = Color(0xFFE5E7EB)
val LightSurfaceContainerHighest = Color(0xFFD1D5DB)

val LightOutline        = Color(0xFFD1D5DB) // Gray-300
val LightOutlineVariant = Color(0xFFE5E7EB) // Gray-200

val LightSecondary            = Color(0xFF6B7280)
val LightOnSecondary          = Color(0xFFFFFFFF)
val LightSecondaryContainer   = Color(0xFFE5E7EB)
val LightOnSecondaryContainer = Color(0xFF374151)

val LightTertiary            = Color(0xFF7E3000)
val LightOnTertiary          = Color(0xFFFFFFFF)
val LightTertiaryContainer   = Color(0xFFA44100)
val LightOnTertiaryContainer = Color(0xFFFFD2BE)

val LightError            = Color(0xFFDB354F)
val LightOnError          = Color(0xFFFFFFFF)
val LightErrorContainer   = Color(0xFFFEE2E2)
val LightOnErrorContainer = Color(0xFFDB354F)

// ─── Dark Theme ──────────────────────────────────────────────────────────────
// Near-black background + same #2563EB primary blue for consistent brand presence

val DarkPrimary            = Color(0xFF2563EB) // same brand blue as light mode
val DarkOnPrimary          = Color(0xFFFFFFFF)
val DarkPrimaryContainer   = Color(0xFF1E3A8A) // blue-900 — container on dark
val DarkOnPrimaryContainer = Color(0xFFDBEAFE) // blue-100
val DarkInversePrimary     = Color(0xFF2563EB)

val DarkBackground         = Color(0xFF0F0F0F) // near-black
val DarkOnBackground       = Color(0xFFF1F1F1) // near-white — high contrast

val DarkSurface            = Color(0xFF1A1A1A) // elevated card surface
val DarkOnSurface          = Color(0xFFF1F1F1) // near-white text
val DarkOnSurfaceVariant   = Color(0xFF9CA3AF) // muted text (Gray-400)
val DarkSurfaceVariant     = Color(0xFF2A2A2A)
val DarkSurfaceTint        = Color(0xFF2563EB)
val DarkInverseSurface     = Color(0xFFF1F1F1)
val DarkInverseOnSurface   = Color(0xFF0F0F0F)

val DarkSurfaceContainerLowest  = Color(0xFF0A0A0A) // deepest — true near-black
val DarkSurfaceContainerLow     = Color(0xFF0F0F0F) // background level
val DarkSurfaceContainer        = Color(0xFF1A1A1A) // default card
val DarkSurfaceContainerHigh    = Color(0xFF232323) // raised card / sheet
val DarkSurfaceContainerHighest = Color(0xFF2D2D2D) // top-most elevation

val DarkOutline        = Color(0xFF2D2D2D) // subtle dividers
val DarkOutlineVariant = Color(0xFF1F1F1F) // faintest border

val DarkSecondary            = Color(0xFFA8B5C8)
val DarkOnSecondary          = Color(0xFF1C2733)
val DarkSecondaryContainer   = Color(0xFF2A3544)
val DarkOnSecondaryContainer = Color(0xFFBCC8D9)

val DarkTertiary            = Color(0xFFF7BD3E)
val DarkOnTertiary          = Color(0xFF402D00)
val DarkTertiaryContainer   = Color(0xFF3D3520)
val DarkOnTertiaryContainer = Color(0xFFFFDEA3)

val DarkError            = Color(0xFFFFB4AB)
val DarkOnError          = Color(0xFF690005)
val DarkErrorContainer   = Color(0xFF3D1518)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// ══════════════════════════════════════════════════════════════════════════════
// SEMANTIC TOKENS — use these in Compose UI (never raw hex)
// ══════════════════════════════════════════════════════════════════════════════

object AppColors {
    // ── Text ─────────────────────────────────────────────────────────────────
    val TextPrimary = Color(0xFF111827)
    val TextMuted   = Color(0xFF6B7280)

    // ── Surfaces ─────────────────────────────────────────────────────────────
    val CardBorder          = Color(0xFFE5E7EB)
    val ProgressTrack       = Color(0xFFE5E7EB)
    val DashboardBackground = Color(0xFFF1F5F9)
    val ShadowAmbient       = Color(0x14000000)
    val ShadowSpot          = Color(0x05000000)

    // ── Primary — #2563EB Royal Blue ─────────────────────────────────────────
    val EmeraldAccent       = Color(0xFF2563EB) // buttons, FAB, active states
    val EmeraldAccentDeep   = Color(0xFF1D4ED8) // blue-700 — pressed
    val EmeraldAccentDark   = Color(0xFF1E40AF) // blue-800 — deep
    val EmeraldAccentLight  = Color(0xFFEFF6FF) // blue-50 — chip / badge bg
    val EmeraldAccentBorder = Color(0xFFBFDBFE) // blue-200 — outlined borders
    val HeroGlow            = Color(0x1A2563EB)

    // ── Success — #1AA47B (paid states, positive amounts) ───────────────────
    val Success          = Color(0xFF1AA47B)
    val SuccessContainer = Color(0xFFE6F7F3)
    val OnSuccess        = Color(0xFF0D5A44)

    // ── Warning ──────────────────────────────────────────────────────────────
    val Warning          = Color(0xFFF59E0B)
    val WarningContainer = Color(0xFFFFFBEB)
    val OnWarning        = Color(0xFF78350F)

    // ── Error ────────────────────────────────────────────────────────────────
    val Error          = Color(0xFFDB354F)
    val ErrorContainer = Color(0xFFFEE2E2)
    val OnError        = Color(0xFFFFFFFF)

    // ── Avatar palette ───────────────────────────────────────────────────────
    val AvatarWarm    = Color(0xFF7C2D12)
    val AvatarNeutral = Color(0xFFF1F5F9)
    val AvatarSuccess = Color(0xFFD1FAE5)
    val AvatarError   = Color(0xFFFEE2E2)
}

// ── Extended hex constants (used in dynamic Color() constructors) ─────────────
object ExtendedColorHex {
    const val VacantBackground = 0xFFFFDAD6L
    const val VacantText       = 0xFFBA1A1AL
    const val VacantBorder     = 0xFFFFB4ABL

    const val OccupiedBackground = 0xFFE6F7F3L
    const val OccupiedText       = 0xFF1AA47BL
    const val OccupiedBorder     = 0xFF99D9CAL

    const val ActiveBackground = 0xFFEFF6FFL // blue-50
    const val ActiveText       = 0xFF2563EBL // primary blue

    const val InactiveBackground = 0xFFF3F4F6L
    const val InactiveText       = 0xFF6B7280L

    val AvatarPairs = listOf(
        Pair(0xFFDBEAFEL, 0xFF2563EBL), // primary blue
        Pair(0xFFE6F7F3L, 0xFF1AA47BL), // success green
        Pair(0xFFE0F2FEL, 0xFF0369A1L),
        Pair(0xFFFDF4FFL, 0xFF7E22CEL),
        Pair(0xFFFFD2BEL, 0xFF7E3000L),
        Pair(0xFFDCE2F7L, 0xFF404758L)
    )
}
