package com.example.team26.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import com.example.team26.ui.station.StationListScreen
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.team26.ui.home.HomeScreen
import com.example.team26.ui.notifications.NotificationScreen
import com.example.team26.ui.user.SettingsScreen
import com.example.team26.ui.station.StationScreen
import com.example.team26.ui.map.MaplibreScreen
import com.example.team26.R
import com.example.team26.navigation.NavigationHelper
import com.example.team26.navigation.Route

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // Track the current route for highlighting the correct nav item
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Route.Home.route


    Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    NavigationHelper.mainNavItems.forEach { navItem ->
                        // Determine if this item is selected based on the route
                        val isSelected = currentRoute == navItem.route.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                // TODO: Remove bottom nav to Station, redundant to have both list and details in bottom nav, just go wherever was left off
                                if (navItem.route == Route.Station) {
                                    // For station button, navigate to the default station
                                    navController.navigate(Route.Station.defaultRoute)
                                } else {
                                    // For other buttons, use the helper
                                    NavigationHelper.navigateTo(navController, navItem.route.route)
                                }
                            },
                            icon = { Icon(imageVector = navItem.icon, contentDescription = navItem.label) },
                            label = { Text(text = navItem.label, maxLines = 1) }
                        )
                    }
                }
            }
        ) { innerPadding -> Box(modifier = Modifier.fillMaxSize()) {
           Image(
                painter = painterResource(id = R.drawable.logo2048x2048),
                contentDescription = "Background image",
                modifier = Modifier
                    .align(Alignment.Center)
                    .absoluteOffset(0.dp, (-30).dp)
           )

        // Navigation host for screen content
        NavHost(
                navController = navController,
                startDestination = Route.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
            composable(Route.Home.route) {
                HomeScreen(modifier)
            }


            composable(
                route = Route.Station.route,
                arguments = listOf(
                    navArgument(Route.Station.ARG_STATION_ID) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                // Extract the station ID from the route
                val stationId = backStackEntry.arguments?.getString(Route.Station.ARG_STATION_ID) ?: "station-1"
                StationScreen(
                    modifier = modifier,
                    stationId = stationId  // Pass as a parameter to StationScreen
                )
            }


            composable(Route.StationsList.route) {
                StationListScreen(
                    modifier = modifier,
                    navController = navController
                )
            }

            composable(Route.Map.route) {
                MaplibreScreen(
                    modifier = modifier,
                    navController = navController
                )
            }

            composable(Route.Notifications.route) {
                NotificationScreen(modifier)
            }

            composable(Route.Settings.route) {
                SettingsScreen(modifier)
            }
        }
    }
    }
}



@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
