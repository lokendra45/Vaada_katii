package com.gaatho.rent.core.environment

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    private var defaultLocale: Locale? = null
    actual val current: String
        @Composable get() = Locale.getDefault().toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = LocalConfiguration.current

        if (defaultLocale == null) {
            defaultLocale = Locale.getDefault()
        }

        val newLocale = if(value == null) {
            defaultLocale!!
        } else {
            Locale.forLanguageTag(value)
        }
        Locale.setDefault(newLocale)
        configuration.setLocale(newLocale)
        
        val context = LocalContext.current
        val newContext = context.createConfigurationContext(configuration)

        return LocalContext provides newContext
    }
}

actual object LocalAppTheme {
    actual val current: Boolean
        @Composable get() = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    @Composable
    actual infix fun provides(value: Boolean?): ProvidedValue<*> {
        val new = if (value == null) {
            LocalConfiguration.current
        } else {
            Configuration(LocalConfiguration.current).apply {
                uiMode = when (value) {
                    true -> (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
                    false -> (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
                }
            }
        }
        return LocalConfiguration.provides(new)
    }
}
