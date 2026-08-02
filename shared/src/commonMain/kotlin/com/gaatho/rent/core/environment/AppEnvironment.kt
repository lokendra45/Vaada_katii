package com.gaatho.rent.core.environment

import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import com.gaatho.rent.database.RentManagerDatabase
import androidx.compose.runtime.LaunchedEffect

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

    // Observe theme directly from SQLDelight Database
    val database: RentManagerDatabase = koinInject()
    val darkModeDbValue by database.rentManagerQueries
        .selectSetting("pref_dark_mode")
        .asFlow()
        .mapToOneOrNull(Dispatchers.IO)
        .collectAsState(initial = null)
        
    val customAppThemeIsDark = darkModeDbValue?.toBooleanStrictOrNull()

    CompositionLocalProvider(
        LocalAppLocale provides languageCode,
        LocalAppTheme provides customAppThemeIsDark,
    ) {
        content()
    }
}

