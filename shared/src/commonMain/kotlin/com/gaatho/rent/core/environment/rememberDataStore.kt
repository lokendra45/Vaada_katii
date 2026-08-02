package com.gaatho.rent.core.environment

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

@Composable
expect fun rememberDataStore(): DataStore<Preferences>
