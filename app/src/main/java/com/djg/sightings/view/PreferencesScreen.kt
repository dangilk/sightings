package com.djg.sightings.view

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.djg.sightings.ui.Dimens.Companion.paddingLarge
import com.djg.sightings.ui.Dimens.Companion.paddingSmall
import com.djg.sightings.ui.formatLatLon
import com.djg.sightings.viewmodel.PreferencesViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel = viewModel(factory = PreferencesViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = paddingLarge),
        verticalArrangement = spacedBy(paddingSmall)
    ) {
        Text(
            "Preferences",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
        )

        if (uiState.currentLat != null && uiState.currentLon != null) {
            Text(
                "Current location: ${formatLatLon(uiState.currentLat!!, uiState.currentLon!!)}",
            )
        }

        TextField(
            value = uiState.username,
            onValueChange = { viewModel.setUsername(it) },
            label = { Text("Username") },
            maxLines = 1
        )

        NotificationToggleWithPermission(
            onPermissionChanged = {
                val enabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it[Manifest.permission.POST_NOTIFICATIONS] ?: false
                } else {
                    throw IllegalStateException("Notification permission not required")
                }
                viewModel.onToggleNotifications(enabled)
            },
            onToggleNotifications = { viewModel.onToggleNotifications(it) },
            notificationsEnabled = uiState.notificationsEnabled,
            onLaunchSettings = { viewModel.onLaunchChannelSettings(it) },
            isNotificationChannelEnabled = { viewModel.isNotificationChannelEnabled() }
        )

        Button(onClick = { viewModel.deleteAllAlerts() }) {
            Text("Delete all local alerts")
        }
    }
}

@Composable
fun ToggleSwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationToggleWithPermission(
    onPermissionChanged: (Map<String, Boolean>) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onLaunchSettings: (Activity) -> Unit,
    isNotificationChannelEnabled: () -> Boolean,
    notificationsEnabled: Boolean
) {

    val notificationPermissionState =
        rememberMultiplePermissionsState(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }, { onPermissionChanged(it) }
        )
    val activity = LocalActivity.current

    ToggleSwitch(
        isChecked = notificationsEnabled && notificationPermissionState.allPermissionsGranted && isNotificationChannelEnabled(),
        onCheckedChange = { enabled ->
            if (!enabled) {
                onToggleNotifications(false)
                return@ToggleSwitch
            }
            if (notificationPermissionState.allPermissionsGranted) {
                if (isNotificationChannelEnabled()) {
                    onToggleNotifications(true)
                } else {
                    onLaunchSettings(activity!!)
                }
            } else {
                notificationPermissionState.launchMultiplePermissionRequest()
            }
        },
        label = "Enable notifications"
    )

}