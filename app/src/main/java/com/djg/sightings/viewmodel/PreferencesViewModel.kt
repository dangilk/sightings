package com.djg.sightings.viewmodel

import android.app.Activity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.djg.sightings.MainApplication
import com.djg.sightings.data.AlertRepository
import com.djg.sightings.data.SettingsKeys
import com.djg.sightings.data.settingsStore
import com.djg.sightings.notification.AlertNotificationManager
import com.djg.sightings.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val ioScope: CoroutineScope,
    private val preferences: DataStore<Preferences>,
    private val savedStateHandle: SavedStateHandle,
    private val alertRepository: AlertRepository,
    private val alertNotificationManager: AlertNotificationManager,
    private val locationService: LocationService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    data class UIState(
        val notificationsEnabled: Boolean = false,
        val currentLat: Double? = null,
        val currentLon: Double? = null,
        val username: String = "",
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PreferencesViewModel(
                    ioScope = (this[APPLICATION_KEY] as MainApplication).ioScope,
                    savedStateHandle = createSavedStateHandle(),
                    preferences = (this[APPLICATION_KEY] as MainApplication).settingsStore,
                    alertRepository = (this[APPLICATION_KEY] as MainApplication).alertRepository,
                    alertNotificationManager = (this[APPLICATION_KEY] as MainApplication).alertNotificationManager,
                    locationService = (this[APPLICATION_KEY] as MainApplication).locationService,
                )
            }
        }
    }

    init {
        ioScope.launch {
            locationService.locationFlow.collect { location ->
                _uiState.update {
                    it.copy(
                        currentLat = location.latitude,
                        currentLon = location.longitude
                    )
                }
            }
        }
        ioScope.launch {
            preferences.data.collect { preferences ->
                _uiState.update {
                    it.copy(
                        notificationsEnabled = preferences[SettingsKeys.NOTIFICATIONS_ENABLED]
                            ?: false
                    )
                }
            }
        }
        ioScope.launch {
            preferences.data.collect { preferences ->
                _uiState.update {
                    it.copy(
                        username = preferences[SettingsKeys.USERNAME] ?: ""
                    )
                }
            }
        }
    }

    fun onToggleNotifications(enabled: Boolean) {
        ioScope.launch {
            preferences.edit { settings ->
                settings[SettingsKeys.NOTIFICATIONS_ENABLED] = enabled
            }
        }
    }

    fun setUsername(username: String) {
        _uiState.update {
            it.copy(username = username)
        }
        ioScope.launch {
            preferences.edit { settings ->
                settings[SettingsKeys.USERNAME] = username
            }
        }
    }

    fun deleteAllAlerts() {
        ioScope.launch {
            alertRepository.deleteAllAlerts()
        }
    }

    fun onLaunchChannelSettings(activity: Activity) {
        alertNotificationManager.launchChannelSettings(activity)
    }

    fun isNotificationChannelEnabled(): Boolean {
        return alertNotificationManager.isChannelEnabled()
    }

}