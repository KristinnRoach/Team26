package com.example.team26.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.MapLibre

@Composable
fun MaplibreScreen(modifier : Modifier = Modifier) {
    val rvk = LatLng(64.147, -21.9408)
    val cameraPosition = rememberSaveable {
        mutableStateOf(CameraPosition(target = rvk, zoom = 15.0))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

    MapLibre(modifier = Modifier.fillMaxSize(),
        styleBuilder = Style.Builder()
            .fromUri("https://tiles.openfreemap.org/styles/liberty"),

        cameraPosition = cameraPosition.value
    )
    }
}


