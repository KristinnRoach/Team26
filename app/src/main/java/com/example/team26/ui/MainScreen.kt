package com.example.team26.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import com.example.team26.ui.station.StationListScreen
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.team26.ui.home.HomeScreen
import com.example.team26.ui.notifications.NotificationScreen
import com.example.team26.ui.user.SettingsScreen
import com.example.team26.ui.station.StationScreen
import com.example.team26.navigation.NavItem
import com.example.team26.ui.map.MaplibreScreen
import com.example.team26.R

@Composable
fun MainScreen(modifier: Modifier = Modifier) { // viewModel: ViewModel

    val navItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Station", Icons.Default.Build),
        NavItem("List", Icons.AutoMirrored.Filled.List),
        NavItem("Map", Icons.Default.LocationOn),
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
                        val destination = when (index) {
                            0 -> "home"
                            1 -> "station"
                            2 -> "stations_list"
                            3 -> "map"
                            4 -> "notifications"
                            5 -> "settings"
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
        ) { innerPadding -> Box(modifier = Modifier.fillMaxSize()) {
           Image(
                painter = painterResource(id = R.drawable.logo2048x2048),
                contentDescription = "Background image",
                modifier = Modifier.run {
                    align(androidx.compose.ui.Alignment.Center)
                                .absoluteOffset(0.dp, (-30).dp) // .scale(scaleX = 1.25f, scaleY = 1.25f)
                },
                    // .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
                    // .blur(5.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                // contentScale = ContentScale.Crop
           )
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeScreen(modifier) }    // navController passed only if the consuming screen needs it
                composable("station") { StationScreen(modifier, navController) } // ViewModel will be automatically injected
                composable("stations_list") { StationListScreen(modifier, navController) }
                composable("map") { MaplibreScreen(modifier, navController) }
                composable("notifications") { NotificationScreen(modifier) }
                composable("settings") { SettingsScreen(modifier) }
            }
        }
    }
    }


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
