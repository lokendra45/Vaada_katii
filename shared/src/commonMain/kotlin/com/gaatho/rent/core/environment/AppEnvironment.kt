package com.gaatho.rent.core.environment

import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import androidx.compose.runtime.LaunchedEffect
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.map

expect object LocalAppLocale {
    val current: String @Composable get
    @Composable infix fun provides(value: String?): ProvidedValue<*>
}

expect object LocalAppTheme {
    val current: Boolean @Composable get
    @Composable infix fun provides(value: Boolean?): ProvidedValue<*>
}

@Composable
fun AppEnvironment(content: @Composable () -> Unit) {
    val viewModel = koinViewModel<LanguageViewModel>()
    val languageCode by viewModel.languageCode.collectAsStateWithLifecycle()

    // Observe theme directly from DataStore
    val dataStore: DataStore<Preferences> = koinInject()
    val darkModeKey = booleanPreferencesKey("pref_dark_mode")
    val darkModeValue by dataStore.data
        .map { it[darkModeKey] }
        .collectAsState(initial = null)

    AppEnvironmentPlatform(
        languageCode = languageCode,
        darkMode = darkModeValue,
        content = content
    )
}

@Composable
expect fun AppEnvironmentPlatform(
    languageCode: String?,
    darkMode: Boolean?,
    content: @Composable () -> Unit
)

