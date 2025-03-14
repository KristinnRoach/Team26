package com.example.team26.utils

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@SuppressLint("MissingPermission")
fun getLastLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
    // Get the last known location
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(location)
            } else {
                // If last location is null, request a current location
                getCurrentLocation(fusedLocationClient, onLocationReceived)
            }
        }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
    // Create cancellation token for the location request
    val cancellationToken = CancellationTokenSource().token

    // Get current location with high accuracy
    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationToken
    ).addOnSuccessListener { location ->
        location?.let {
            onLocationReceived(it)
        }
    }
}
