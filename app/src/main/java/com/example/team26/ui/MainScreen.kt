package com.example.team26.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import com.example.team26.ui.station.StationListScreen
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.team26.ui.home.HomeScreen
import com.example.team26.ui.notifications.NotificationScreen
import com.example.team26.ui.user.SettingsScreen
import com.example.team26.navigation.NavItem
import com.example.team26.R
import com.example.team26.di.AppModule
import com.example.team26.ui.station.StationViewModel
import com.example.team26.viewModel.NotificationViewModel
import com.example.team26.viewModel.viewModelFactory
import com.example.team26.ui.map.SimpleMapScreen
import com.example.team26.utils.ForegroundStateChecker
import kotlinx.coroutines.launch

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Stations", Icons.AutoMirrored.Filled.List),
        NavItem("Map", Icons.Default.LocationOn),
        NavItem("Notifs", Icons.Default.Notifications),
        NavItem("Settings", Icons.Default.Settings),
    )
    var selectedDestination by remember { mutableStateOf("home") }
    val navController = rememberNavController()

    val stationRepository = AppModule.stationRepository
    val notificationDao = AppModule.notificationDao
    val snackbarHostState = remember { SnackbarHostState() }

    // ViewModels
    val stationViewModel: StationViewModel = viewModel(
        factory = viewModelFactory { StationViewModel(stationRepository, notificationDao) }
    )
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = viewModelFactory { NotificationViewModel(notificationDao) }
    )
    val unreadNotificationCount by notificationViewModel.unreadCount.collectAsState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val foregroundChecker = remember {
        ForegroundStateChecker(
            lifecycleOwner = lifecycleOwner,
            stationRepository = stationRepository,
            notificationDao = notificationDao,
            stationViewModel = stationViewModel,
            showSnackbar = { message, actionLabel ->
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel,
                    duration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed && actionLabel == "View") {
                    coroutineScope.launch {
                        selectedDestination = "notifications"
                        navController.navigate("notifications") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }
                result
            }
        )
    }

    LaunchedEffect(foregroundChecker) {
        Log.d("MainScreen", "Starting ForegroundStateChecker...")
        foregroundChecker.start()
    }

    val selectedStationId by stationViewModel.selectedStationId.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (selectedDestination == "stations" && selectedStationId != null) {
                CenterAlignedTopAppBar(
                    title = { Text("Station Details") },
                    navigationIcon = {
                        IconButton(onClick = { stationViewModel.setStationId(null) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Stations List"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    val destination = when (item.label) {
                        "Home" -> "home"
                        "Stations" -> "stations"
                        "Map" -> "map"
                        "Notifs" -> "notifications"
                        "Settings" -> "settings"
                        else -> "home"
                    }
                    val isNotificationItem = item.label == "Notifs"
                    NavigationBarItem(
                        selected = selectedDestination == destination,
                        onClick = {
                            if (destination == "stations" && selectedDestination == "stations" && selectedStationId != null) {
                                stationViewModel.setStationId(null)
                            }
                            selectedDestination = destination
                            navController.navigate(destination) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (isNotificationItem && unreadNotificationCount > 0) {
                                BadgedBox(badge = { Badge { Text("$unreadNotificationCount") } }) {
                                    Icon(imageVector = item.icon, contentDescription = item.label)
                                }
                            } else {
                                Icon(imageVector = item.icon, contentDescription = item.label)
                            }
                        },
                        label = { Text(text = item.label, maxLines = 1) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.logo2048x2048),
                contentDescription = "Background image",
                modifier = Modifier.run {
                    align(androidx.compose.ui.Alignment.Center)
                        .absoluteOffset(0.dp, (-30).dp)
                },
                alpha = if (selectedDestination == "stations" && selectedStationId != null) 0.3f else 1.0f
            )

            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeScreen(modifier) }
                composable("stations") {
                    StationListScreen(
                        modifier,
                        navController,
                        stationViewModel
                    )
                }
                // composable("map") { MaplibreScreen(modifier, navController, stationViewModel) }
                composable("map") {
                    SimpleMapScreen(
                        modifier = modifier, 
                        stationViewModel = stationViewModel, 
                        navController = navController,
                        onDestinationChanged = { newDestination ->
                            selectedDestination = newDestination
                        }
                    )
                }
                composable("notifications") { NotificationScreen(modifier, notificationViewModel) }
                composable("settings") { SettingsScreen(modifier) }
            }
        }
    }
}
