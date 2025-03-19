package com.example.team26.ui.station

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.team26.viewModel.viewModelFactory
import androidx.navigation.NavHostController
import com.example.team26.di.AppModule

@Composable
fun StationScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    stationId: String = "station-1",
    viewModel: StationViewModel = viewModel(
        factory = viewModelFactory {
            StationViewModel(
                AppModule.stationRepository,
            )
        }
    ))
 {

     val uiState by viewModel.uiState.collectAsState()

     LaunchedEffect(Unit) {
         // TEMP HACK
         val stationId = TempStorage.selectedStationId

         viewModel.setStationId(stationId)
         viewModel.refreshStation()
     }

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null && uiState.stationName.isEmpty()) {
            // Show error if we have an error and no station data
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error: ${uiState.errorMessage}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { viewModel.refreshStation() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Retry")
                    }

                }
            }
        } else {
            // Show station details
            StationDetails(
                uiState = uiState,
                onRefresh = { viewModel.refreshStation() },
                onToggleState = { viewModel.toggleStationState() },
                onToggleMode = { viewModel.toggleControlMode() }
            )
        }
    }
}

@Composable
fun StationDetails(
    uiState: StationDetailUiState,
    onRefresh: () -> Unit,
    onToggleState: () -> Unit,
    onToggleMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with station name and status indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.stationName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${uiState.stationId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when (uiState.status) {
                            "ON" -> Color.Green
                            "OFF" -> Color.Red
                            else -> Color.Gray
                        }
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error message if any
        if (uiState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = uiState.errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Station details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Station Details",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Column {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Lat: ${uiState.latitude}, Lng: ${uiState.longitude}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Last updated
                DetailRow(title = "Last Updated", value = uiState.lastUpdated)

                Spacer(modifier = Modifier.height(16.dp))

                // Controls section
                Text(
                    text = "Controls",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Power state control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Power",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = uiState.status,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = uiState.status == "ON",
                        onCheckedChange = { onToggleState() },
                        enabled = uiState.canToggleState,
                        modifier = Modifier.padding(8.dp)
                    )

                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Control Mode",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = uiState.controlMode,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = uiState.controlMode == "ASTRO",
                        onCheckedChange = { onToggleMode() },
                        enabled = uiState.canToggleMode
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Refresh button
        Button(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh"
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Refresh")
        }

    }
}

@Composable
fun DetailRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

