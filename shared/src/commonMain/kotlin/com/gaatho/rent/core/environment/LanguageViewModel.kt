package com.gaatho.rent.core.environment

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LanguageViewModel(
    private val dataStore: DataStore<Preferences>
): ViewModel() {

    private val languageCodeKey = stringPreferencesKey("languageCode")

    val languageCode = dataStore
        .data
        .map { prefs -> prefs[languageCodeKey] }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            getDefaultLocale()
        )

    fun switchLanguage(languageCode: String) {
        viewModelScope.launch {
            dataStore.edit { mutablePrefs ->
                mutablePrefs[languageCodeKey] = languageCode
            }
        }
    }
}
