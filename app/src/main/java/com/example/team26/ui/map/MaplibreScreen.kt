package com.example.team26.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.rememberStyleState
import dev.sargunv.maplibrecompose.core.GestureSettings
import dev.sargunv.maplibrecompose.core.OrnamentSettings
import dev.sargunv.maplibrecompose.material3.controls.AttributionButton
import dev.sargunv.maplibrecompose.material3.controls.DisappearingCompassButton
import dev.sargunv.maplibrecompose.material3.controls.DisappearingScaleBar
import io.github.dellisd.spatialk.geojson.Position
import dev.sargunv.maplibrecompose.core.CameraPosition
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlin.time.Duration.Companion.milliseconds
import com.example.team26.utils.PermissionHandler
import com.example.team26.utils.getCurrentLocation
import com.example.team26.utils.getLastLocation

// When testing on emulator, set useTestLocation to true and provide your test coordinates
val useTestLocation = true
val testLatitude = 64.136181 // Gróska
val testLongitude = -21.946960

@Composable
fun MaplibreScreen(modifier : Modifier = Modifier, navController : NavHostController) {
// Use dev.sargunv.maplibrecompose for reference

    // Default position (Reykjavik) as fallback
    val defaultPosition = Position(-21.9408, 64.147)

    // State to hold user location
    var userPosition by remember { mutableStateOf(defaultPosition) }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }


    // Create camera state
    val cameraPosition = CameraPosition(
        target = defaultPosition,
        zoom = 14.0,
        bearing = 0.0,  // Bearing (rotation) in degrees
        tilt = 0.0,     // Tilt angle in degrees
        padding = PaddingValues(0.dp) // center point can be altered with padding
    )

    val camera = rememberCameraState(cameraPosition)
    val styleState = rememberStyleState()

    // Wrap map in the permission handler
    PermissionHandler(
        modifier = modifier,
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        description = "Location permission is needed to show your position on the map",
        onGranted = {
            // Update the camera when we get a new position
            LaunchedEffect(userPosition) {
                camera.animateTo(
                    CameraPosition(
                        target = userPosition,
                        zoom = 15.0
                    ),
                    duration = 700.milliseconds
                )
            }

            // Get location once permission is granted
            LaunchedEffect(Unit) {
                // Use the test location if testing on emulator
                if (useTestLocation) {
                    val mockLocation = Location("MockProvider").apply {
                        latitude = testLatitude
                        longitude = testLongitude
                    }
                    userPosition = Position(mockLocation.longitude, mockLocation.latitude)
                } else {
                    getLastLocation(fusedLocationClient) { location ->
                        userPosition = Position(location.longitude, location.latitude)
                    }
                }
            }

            // Render the map
            Box(Modifier.fillMaxSize()) {
                MaplibreMap(
                    cameraState = camera,
                    styleUri = "https://tiles.openfreemap.org/styles/liberty",
                    styleState = rememberStyleState(),
                    ornamentSettings = OrnamentSettings(
                        isLogoEnabled = false,
                        isAttributionEnabled = true,
                        isCompassEnabled = true,
                        isScaleBarEnabled = true,
                    ),
                    gestureSettings =
                        GestureSettings(
                            isTiltGesturesEnabled = true,
                            isZoomGesturesEnabled = true,
                            isRotateGesturesEnabled = true,
                            isScrollGesturesEnabled = true,
                        )
                )

                Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    DisappearingScaleBar(
                        metersPerDp = camera.metersPerDpAtTarget,
                        zoom = camera.position.zoom,
                        modifier = Modifier.align(Alignment.TopStart),
                    )
                    DisappearingCompassButton(
                        camera,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                    AttributionButton(styleState, modifier = Modifier.align(Alignment.BottomEnd))
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun MaplibreScreenPreview() {
    MaplibreScreen(navController = NavHostController(LocalContext.current))
}


