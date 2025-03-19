package com.example.team26.navigation

/**
 * Sealed class to define all application routes and their parameters,
 * providing type safety and centralized route management.
 */
sealed class Route(val route: String) {
    // Main destinations
    object Home : Route("home")
    object Station : Route("station") // Simplified for now
    object StationsList : Route("stations_list")
    object Map : Route("map")
    object Notifications : Route("notifications")
    object Settings : Route("settings")

    // List of all main navigation destinations (for the bottom nav bar)
    companion object {
        val mainDestinations = listOf(
            Home,
            Station,
            StationsList,
            Map,
            Notifications,
            Settings
        )
    }
}

/*
/**
 * Sealed class to define all application routes and their parameters,
 * providing type safety and centralized route management.
 */
sealed class Route(val route: String) {
    // Main destinations
    object Home : Route("home")
    object StationsList : Route("stations_list")
    object Map : Route("map")
    object Notifications : Route("notifications")
    object Settings : Route("settings")

    // Destinations with parameters
    object Station : Route("station/{stationId}") {
        const val ARG_STATION_ID = "stationId"

        // Helper function to create the route with parameters
        fun createRoute(stationId: String): String {
            return "station/$stationId"
        }

        // Helper function to create the route with default parameter
        fun createRoute(): String {
            return "station/station-1" // Default station if none specified
        }
    }

    // List of all main navigation destinations (for the bottom nav bar)
    companion object {
        val mainDestinations = listOf(
            Home,
            Station,
            StationsList,
            Map,
            Notifications,
            Settings
        )
    }
}

 */