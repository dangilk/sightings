package com.djg.sightings.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.djg.sightings.MainApplication
import com.djg.sightings.data.Alert
import com.djg.sightings.data.AlertRepository
import com.djg.sightings.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModel(
    private val alertRepository: AlertRepository,
    private val locationService: LocationService,
    private val ioScope: CoroutineScope,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    data class UIState(
        val alerts: List<Alert> = emptyList(),
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AlertsViewModel(
                    alertRepository = (this[APPLICATION_KEY] as MainApplication).alertRepository,
                    locationService = (this[APPLICATION_KEY] as MainApplication).locationService,
                    ioScope = (this[APPLICATION_KEY] as MainApplication).ioScope,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }

    init {
        ioScope.launch {
            locationService.locationFlow.flatMapLatest { location ->
                alertRepository.alertsWithinRadius(
                    lat = location.latitude,
                    lon = location.longitude,
                    radiusKm = 25.0
                )
            }.collect { alerts ->
                _uiState.update { it.copy(alerts = alerts) }
            }
        }
    }

    fun onClickAlert(alert: Alert) {
        ioScope.launch {
            alertRepository.updateAlert(alert.copy(isRead = true))
        }
    }


    fun onLocationPermissionChanged(granted: Boolean) {
        if (granted) {
            locationService.startLocationUpdates()
        }
    }
}