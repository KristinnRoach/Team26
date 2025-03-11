package com.example.team26.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.rememberStyleState
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.core.GestureSettings
import dev.sargunv.maplibrecompose.core.OrnamentSettings
import dev.sargunv.maplibrecompose.material3.controls.AttributionButton
import dev.sargunv.maplibrecompose.material3.controls.CompassButton
import dev.sargunv.maplibrecompose.material3.controls.DisappearingCompassButton
import dev.sargunv.maplibrecompose.material3.controls.DisappearingScaleBar
import dev.sargunv.maplibrecompose.material3.controls.ScaleBar
import io.github.dellisd.spatialk.geojson.Position
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import kotlin.time.Duration.Companion.seconds

// import org.ramani.compose.CameraPosition
// import org.ramani.compose.MapLibre

@Composable
fun MaplibreScreen(modifier : Modifier = Modifier) {
    val rvk = LatLng(64.147, -21.9408)
    val rvkLat = 64.147
    val rvkLng = -21.9408


    val cameraState = rememberCameraState()
    val styleState = rememberStyleState()

    Box(Modifier.fillMaxSize()) {
        MaplibreMap(
            cameraState = cameraState,
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
                metersPerDp = cameraState.metersPerDpAtTarget,
                zoom = cameraState.position.zoom,
                modifier = Modifier.align(Alignment.TopStart),
            )
            DisappearingCompassButton(cameraState, modifier = Modifier.align(Alignment.TopEnd))
            AttributionButton(styleState, modifier = Modifier.align(Alignment.BottomEnd))
        }
    }
}



