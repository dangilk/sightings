package com.djg.sightings.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsStore by preferencesDataStore(name = "settings")

object SettingsKeys {
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("alerts_enabled")
    val USERNAME = stringPreferencesKey("username")
}
