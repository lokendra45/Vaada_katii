package com.gaatho.rent.core.environment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.SystemTheme
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

@OptIn(InternalComposeUiApi::class)
actual object LocalAppLocale {
    private const val LANG_KEY = "AppleLanguages"
    private val default = NSLocale.preferredLanguages.first() as String
    private val LocalAppLocale = staticCompositionLocalOf { default }
    actual val current: String
        @Composable get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val new = value ?: default
        if (value == null) {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(LANG_KEY)
        } else {
            NSUserDefaults.standardUserDefaults.setObject(arrayListOf(new), LANG_KEY)
        }
        return LocalAppLocale.provides(new)
    }
}

@OptIn(InternalComposeUiApi::class)
actual object LocalAppTheme {
    actual val current: Boolean
        @Composable get() = LocalSystemTheme.current == SystemTheme.Dark

    @Composable
    actual infix fun provides(value: Boolean?): ProvidedValue<*> {
        val new = when(value) {
            true -> SystemTheme.Dark
            false -> SystemTheme.Light
            null -> LocalSystemTheme.current
        }

        return LocalSystemTheme.provides(new)
    }
}
