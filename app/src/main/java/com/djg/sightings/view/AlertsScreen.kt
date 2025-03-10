package com.djg.sightings.view

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.djg.sightings.data.Alert
import com.djg.sightings.data.createMockAlerts
import com.djg.sightings.ui.Dimens.Companion.paddingLarge
import com.djg.sightings.ui.Dimens.Companion.paddingSmall
import com.djg.sightings.ui.formatLatLon
import com.djg.sightings.ui.formatTimestamp
import com.djg.sightings.ui.theme.SightingsTheme
import com.djg.sightings.viewmodel.AlertsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel(factory = AlertsViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = paddingLarge)
    ) {
        Text("Alerts", style = MaterialTheme.typography.titleMedium)
        LocationPermission(viewModel = viewModel)
        AlertList(alerts = uiState.alerts, onClick = { viewModel.onClickAlert(it) })
    }
}

@Composable
private fun AlertList(alerts: List<Alert>, onClick: (Alert) -> Unit) {
    LazyColumn {
        items(alerts) { alert ->
            AlertItem(alert, onClick)
        }
    }
}

@Composable
private fun AlertItem(alert: Alert, onClick: (Alert) -> Unit) {
    Row(modifier = Modifier.clickable { onClick(alert) }) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Read",
            modifier = Modifier
                .padding(paddingSmall)
                .alpha(if (alert.isRead) 1f else 0f),
        )

        Column(
            modifier = Modifier
                .padding(paddingSmall)
                .alpha(if (alert.isRead) 0.5f else 1f)
        ) {
            Text(
                text = alert.title,
                modifier = Modifier,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = formatTimestamp(alert.date),
                modifier = Modifier.padding(start = paddingSmall),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = formatLatLon(alert.latitude, alert.longitude),
                modifier = Modifier.padding(start = paddingSmall),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationPermission(viewModel: AlertsViewModel) {

    // Location permission state
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION, {
            viewModel.onLocationPermissionChanged(it)
        }
    )

    if (!locationPermissionState.status.isGranted) {
        Column {
            val textToShow = if (locationPermissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "Location data is important for this app. Please grant permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Location permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow)
            Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                Text("Grant permission")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAlertList() {
    SightingsTheme {
        AlertList(
            alerts = createMockAlerts(
                centerLat = 0.0,
                centerLon = 0.0,
                radius = 40,
                count = 10
            ),
            onClick = {}
        )
    }
}