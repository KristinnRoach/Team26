package com.example.team26.ui

import StationListScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.team26.ui.home.HomeScreen
import com.example.team26.ui.notifications.NotificationScreen
import com.example.team26.ui.user.SettingsScreen
import com.example.team26.ui.station.StationScreen
import com.example.team26.navigation.NavItem

@Composable
fun MainScreen(viewModel: ViewModel, modifier: Modifier = Modifier) {

    val navItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Station", Icons.Default.Build),
        NavItem("List", Icons.AutoMirrored.Filled.List),
        NavItem("Notifications", Icons.Default.Notifications),
        NavItem("Settings", Icons.Default.Settings),
    )

    var selectedDestination by remember { mutableStateOf("home") }
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    val destination = when(index) {
                        0 -> "home"
                        1 -> "station"
                        2 -> "stations_list"
                        3 -> "notifications"
                        4 -> "settings"
                        else -> "home"
                    }

                    NavigationBarItem(
                        selected = selectedDestination == destination,
                        onClick = {
                            selectedDestination = destination
                            navController.navigate(destination) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                        label = { Text(text = item.label, maxLines = 1) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(modifier, navController) }    // navController passed only if the consuming screen needs it
            composable("station") { StationScreen(modifier, navController) } // ViewModel will be automatically injected
            composable("stations_list") { StationListScreen(modifier, navController) }
            composable("notifications") { NotificationScreen(modifier) }
            composable("settings") { SettingsScreen(modifier) }
        }
    }
}
