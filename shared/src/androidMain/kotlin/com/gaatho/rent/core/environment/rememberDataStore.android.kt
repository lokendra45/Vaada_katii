package com.gaatho.rent.core.environment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

@Composable
actual fun rememberDataStore(): DataStore<Preferences> {
    val context = LocalContext.current
    return remember {
        createDataStore(context)
    }
}
