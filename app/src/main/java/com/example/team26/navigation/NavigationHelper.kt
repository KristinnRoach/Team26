package com.example.team26.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Helper object for navigation-related functionality.
 */
object NavigationHelper {

    /**
     * The list of main navigation items for the bottom navigation bar.
     */
    val mainNavItems = listOf(
        NavItem("Home", Icons.Default.Home, Route.Home),
        NavItem("Station", Icons.Default.Build, Route.Station),
        NavItem("List", Icons.AutoMirrored.Filled.List, Route.StationsList),
        NavItem("Map", Icons.Default.LocationOn, Route.Map),
        NavItem("Notifications", Icons.Default.Notifications, Route.Notifications),
        NavItem("Settings", Icons.Default.Settings, Route.Settings)
    )

    /**
     * Navigates to the specified route
     *
     * @param navController The navigation controller to use for navigation
     * @param route The route to navigate to
     */
    fun navigateTo(navController: NavController, route: String) {
        navController.navigate(route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination
            launchSingleTop = true
            // Restore state when navigating back to a previously visited item
            restoreState = true
        }
    }

    /**
     * Navigate to the station details screen with a specific station ID.
     *
     * @param navController The navigation controller
     * @param stationId The ID of the station to navigate to
     */
    fun navigateToStation(navController: NavController, stationId: String) {
        navController.navigate(Route.Station.createRoute(stationId))
        // navController.navigate(Route.Station.route)
    }

}