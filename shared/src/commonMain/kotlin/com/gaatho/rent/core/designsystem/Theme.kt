package com.gaatho.rent.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EmeraldDarkColorScheme = darkColorScheme(
    primary = EmeraldDarkPrimary,
    onPrimary = EmeraldDarkOnPrimary,
    primaryContainer = EmeraldDarkPrimaryContainer,
    onPrimaryContainer = EmeraldDarkOnPrimaryContainer,
    inversePrimary = EmeraldDarkInversePrimary,
    secondary = EmeraldDarkSecondary,
    onSecondary = EmeraldDarkOnSecondary,
    secondaryContainer = EmeraldDarkSecondaryContainer,
    onSecondaryContainer = EmeraldDarkOnSecondaryContainer,
    tertiary = EmeraldDarkTertiary,
    onTertiary = EmeraldDarkOnTertiary,
    tertiaryContainer = EmeraldDarkTertiaryContainer,
    onTertiaryContainer = EmeraldDarkOnTertiaryContainer,
    error = EmeraldDarkError,
    onError = EmeraldDarkOnError,
    errorContainer = EmeraldDarkErrorContainer,
    onErrorContainer = EmeraldDarkOnErrorContainer,
    background = EmeraldDarkBackground,
    onBackground = EmeraldDarkOnBackground,
    surface = EmeraldDarkSurface,
    onSurface = EmeraldDarkOnSurface,
    surfaceVariant = EmeraldDarkSurfaceVariant,
    onSurfaceVariant = EmeraldDarkOnSurfaceVariant,
    inverseSurface = EmeraldDarkInverseSurface,
    inverseOnSurface = EmeraldDarkInverseOnSurface,
    outline = EmeraldDarkOutline,
    outlineVariant = EmeraldDarkOutlineVariant,
)

private val EmeraldLightColorScheme = lightColorScheme(
    primary = EmeraldLightPrimary,
    onPrimary = EmeraldLightOnPrimary,
    primaryContainer = EmeraldLightPrimaryContainer,
    onPrimaryContainer = EmeraldLightOnPrimaryContainer,
    inversePrimary = EmeraldLightInversePrimary,
    secondary = EmeraldLightSecondary,
    onSecondary = EmeraldLightOnSecondary,
    secondaryContainer = EmeraldLightSecondaryContainer,
    onSecondaryContainer = EmeraldLightOnSecondaryContainer,
    tertiary = EmeraldLightTertiary,
    onTertiary = EmeraldLightOnTertiary,
    tertiaryContainer = EmeraldLightTertiaryContainer,
    onTertiaryContainer = EmeraldLightOnTertiaryContainer,
    error = EmeraldLightError,
    onError = EmeraldLightOnError,
    errorContainer = EmeraldLightErrorContainer,
    onErrorContainer = EmeraldLightOnErrorContainer,
    background = EmeraldLightBackground,
    onBackground = EmeraldLightOnBackground,
    surface = EmeraldLightSurface,
    onSurface = EmeraldLightOnSurface,
    surfaceVariant = EmeraldLightSurfaceVariant,
    onSurfaceVariant = EmeraldLightOnSurfaceVariant,
    inverseSurface = EmeraldLightInverseSurface,
    inverseOnSurface = EmeraldLightInverseOnSurface,
    outline = EmeraldLightOutline,
    outlineVariant = EmeraldLightOutlineVariant,
)

@Composable
fun RentManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        EmeraldDarkColorScheme
    } else {
        EmeraldLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = rentManagerTypography(),
        content = content
    )
}
