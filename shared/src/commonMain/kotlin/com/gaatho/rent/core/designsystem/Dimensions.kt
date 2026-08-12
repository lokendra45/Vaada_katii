package com.gaatho.rent.core.designsystem

import androidx.compose.ui.unit.dp

/**
 * Quiet Premium Spacing & Shape Tokens
 * Source: design-tokens.yaml → spacing + rounded sections
 *
 * 8pt grid system. All values are multiples of 4dp.
 */
object Spacing {
    /** Figma Spec Spacing Scale */
    val Scale2          = 2.dp
    val Scale4          = 4.dp
    val Scale6          = 6.dp
    val Scale8          = 8.dp
    val Scale10         = 10.dp
    val Scale12         = 12.dp
    val Scale16         = 16.dp
    val Scale20         = 20.dp
    val Scale24         = 24.dp
    val Scale28         = 28.dp

    /** 20dp — standard screen edge padding (Figma spec) */
    val ScreenPadding   = 20.dp
    /** 24dp — separation between high-level sections */
    val SectionGap      = 24.dp
    /** 12dp — separation between list items */
    val ItemGap         = 12.dp
    /** 8dp — gap between Title and Description (Hierarchy spec) */
    val StackTight      = 8.dp
    /** 16dp — loose stacking within a component */
    val StackLoose      = 16.dp
}

/**
 * Corner radius tokens (rem → dp at 16px/rem base).
 */
object Radius {
    /** 9-10px — Small element (inputs, small cards) */
    val Sm      = 10.dp
    /** 12px — Figma dashboard buttons and compact controls */
    val Button  = 12.dp
    /** 14px — Medium radius (cards, containers) */
    val Default = 14.dp
    /** 14px — Medium radius (cards, containers) */
    val Md      = 14.dp
    /** 50px — Large radius (tags, badges) */
    val Lg      = 50.dp
    /** 24dp — extra-large cards (optional) */
    val Xl      = 24.dp
    /** 100px+ — Full round (avatars, pills) */
    val Full    = 100.dp
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
    val TextFieldCornerRadius   get() = Radius.Sm
    val radiusLg                get() = Radius.Lg
    val RadiusLarge             get() = Radius.Lg
    val radiusXl                get() = Radius.Xl
    val radiusFull              get() = Radius.Full
    val RadiusPill              get() = Radius.Full

    // ── Component sizes ───────────────────────────────────────────────────
    val TextFieldBorderWidth       get() = 1.dp
    val TextFieldHeight            get() = 50.dp
    val TextFieldHorizontalPadding get() = 14.dp
    val TextFieldVerticalPadding   get() = Spacing.Scale16
    val FieldLabelGap              get() = Spacing.Scale8
    val ButtonHeightMedium         get() = 44.dp
    val ButtonHeightLarge          get() = 45.dp
    val ActionButtonRadius         get() = Radius.Button
}
