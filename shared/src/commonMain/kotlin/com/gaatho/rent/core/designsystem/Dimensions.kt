package com.gaatho.rent.core.designsystem

import androidx.compose.ui.unit.dp

/**
 * Quiet Premium Spacing & Shape Tokens
 * Source: design-tokens.yaml → spacing + rounded sections
 *
 * 8pt grid system. All values are multiples of 4dp.
 */
object Spacing {
    /** 8dp — base grid unit */
    val BaseUnit        = 8.dp
    /** 20dp — standard screen edge padding */
    val ScreenPadding   = 20.dp
    /** 24dp — separation between high-level sections */
    val SectionGap      = 24.dp
    /** 16dp — separation between items/cards within a section */
    val ItemGap         = 16.dp
    /** 4dp — tight label/icon stacking */
    val StackTight      = 4.dp
    /** 12dp — loose stacking within a component */
    val StackLoose      = 12.dp
}

/**
 * Corner radius tokens (rem → dp at 16px/rem base).
 */
object Radius {
    /** 4dp — small chips, tags */
    val Sm      = 4.dp
    /** 8dp — default subtle rounding */
    val Default = 8.dp
    /** 12dp — cards, buttons, inputs (primary usage) */
    val Md      = 12.dp
    /** 16dp — large containers, bottom sheets */
    val Lg      = 16.dp
    /** 24dp — extra-large cards */
    val Xl      = 24.dp
    /** 9999dp — pill shape for badges and filter chips */
    val Full    = 9999.dp
}

/**
 * Backward-compatibility alias for screens that reference AppDimensions.
 * Prefer using [Spacing] and [Radius] directly in new code.
 */
object AppDimensions {
    // ── Spacing ───────────────────────────────────────────────────────────
    val screenPadding           get() = Spacing.ScreenPadding
    val ScreenHorizontalPadding get() = Spacing.ScreenPadding
    val sectionGap              get() = Spacing.SectionGap
    val itemGap                 get() = Spacing.ItemGap
    val PaddingSmall            get() = Spacing.StackTight
    val stackTight              get() = Spacing.StackTight
    val stackLoose              get() = Spacing.StackLoose

    // ── Radius ────────────────────────────────────────────────────────────
    val radiusSm                get() = Radius.Sm
    val RadiusSmall             get() = Radius.Sm
    val radiusMd                get() = Radius.Md
    val RadiusMedium            get() = Radius.Md
    val TextFieldCornerRadius   get() = Radius.Md
    val radiusLg                get() = Radius.Lg
    val RadiusLarge             get() = Radius.Lg
    val radiusXl                get() = Radius.Xl
    val radiusFull              get() = Radius.Full
    val RadiusPill              get() = Radius.Full

    // ── Component sizes ───────────────────────────────────────────────────
    val TextFieldBorderWidth       get() = 1.dp
    val TextFieldHorizontalPadding get() = Spacing.ItemGap   // 16dp
    val TextFieldVerticalPadding   get() = Spacing.StackLoose // 12dp
    val ButtonHeightMedium         get() = 48.dp
    val ButtonHeightLarge          get() = 56.dp
}
