package com.djg.sightings.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.djg.sightings.view.AlertsScreen
import com.djg.sightings.view.PreferencesScreen


sealed class NavRoute(val route: String, val icon: ImageVector, val title: String) {
    data object Alerts : NavRoute("alerts", Icons.Filled.Notifications, "Alerts")
    data object Preferences : NavRoute("preferences", Icons.Filled.Person, "Preferences")
}
@Composable
fun MainNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = NavRoute.Alerts.route, modifier = modifier) {
        composable(NavRoute.Alerts.route) {
            AlertsScreen()
        }
        composable(NavRoute.Preferences.route) {
            PreferencesScreen()
        }
    }
}