package com.example.team26.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.team26.ui.station.StationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import kotlinx.coroutines.launch

@Composable
fun SimpleMapScreen(
    modifier: Modifier = Modifier,
    stationViewModel: StationViewModel,
    navController: NavHostController
) {

    // Collect station data as state
    val allStations by stationViewModel.allStationsUiState.collectAsState()
    val selectedStationId by stationViewModel.selectedStationId.collectAsState()

    // Center map on Reykjavik
    val reykjavik = LatLng(64.14, -21.94)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(reykjavik, 13.5f)
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedStationId) {
        // Find the selected station
        val selected = allStations.find { it.stationId == selectedStationId } ?: return@LaunchedEffect

        val pos = getLatLng(selected.latitude, selected.longitude)
        // Animate camera to selected station
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(pos, 17f),
            1000
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            // todo: testa og velja
            zoomControlsEnabled = true,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            scrollGesturesEnabledDuringRotateOrZoom = true,
            tiltGesturesEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = true,
            mapToolbarEnabled = true,
            rotationGesturesEnabled = true
        ),
        properties = MapProperties(
            mapType = MapType.TERRAIN, // Or SATELLITE, HYBRID, etc.
            isTrafficEnabled = true
            // Todo: create map_style.json in res/raw
           // mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
           // LocalContext.current, R.raw.map_style

    )
) {
        // Add markers for all stations
        allStations.forEach { station ->
            val lat = station.latitude.toDoubleOrNull() ?: 0.0
            val lng = station.longitude.toDoubleOrNull() ?: 0.0
            val isSelected = station.stationId == selectedStationId

            val iconColor = when {
                isSelected -> BitmapDescriptorFactory.HUE_BLUE  // Selected station is blue
                station.status == "ON" -> BitmapDescriptorFactory.HUE_GREEN
                station.status == "OFF" -> BitmapDescriptorFactory.HUE_RED
                else -> BitmapDescriptorFactory.HUE_YELLOW
            }

            MarkerInfoWindowContent(
                state = MarkerState(getLatLng(station.latitude, station.longitude)),
                icon = BitmapDescriptorFactory.defaultMarker(
                    when {
                        station.stationId == selectedStationId -> BitmapDescriptorFactory.HUE_BLUE
                        station.status == "ON" -> BitmapDescriptorFactory.HUE_GREEN
                        station.status == "OFF" -> BitmapDescriptorFactory.HUE_RED
                        else -> BitmapDescriptorFactory.HUE_YELLOW
                    }
                ),
                onInfoWindowLongClick = {
                    // todo: move bottom bar to stations, add topbar & back button
                   //set  previousDestination = "map"
                    navController.navigate("stations") {
                        // clears the back stack up to the map screen
                        popUpTo("map") { inclusive = true }
                        // ensures a new instance of the map screen is created when navigating back from the bottom nav
                        launchSingleTop = true
                    }
                },
                onClick = {
                    stationViewModel.setStationId(station.stationId)
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(getLatLng(station.latitude, station.longitude), 17f),
                            1000
                        )
                    }
                    false // Return false to show the info window
                }
            ) {
                Box(  // info popup
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = station.stationName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Status: ${station.status}\nControl Mode: ${station.controlMode}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = when(station.status) {
                                "ON" -> MaterialTheme.colorScheme.primary
                                "OFF" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

// Convenience method for now, look into changing the data type in the station entity
fun getLatLng(latStr: String, lngStr: String): LatLng {
    val lat = latStr.toDoubleOrNull() ?: 0.0
    val lng = lngStr.toDoubleOrNull() ?: 0.0
    return LatLng(lat, lng)
}
