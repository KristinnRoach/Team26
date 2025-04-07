package com.example.team26.ui.station

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
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
import androidx.navigation.NavHostController

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun StationScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: StationViewModel,
) {
    val selectedStationId by viewModel.selectedStationId.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(selectedStationId) {
        if (selectedStationId != null) {
            viewModel.refreshStation()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        if (selectedStationId == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Please select a station from the list.")
            }
        } else {
            if (uiState.isLoading && uiState.stationId != selectedStationId) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage != null) {
                StationDetails(
                    uiState = uiState,
                    onRefresh = { viewModel.refreshStation() },
                    onToggleState = { viewModel.toggleStationState() },
                    onToggleMode = { viewModel.toggleControlMode() }
                )
            } else if (uiState.isLoading && uiState.stationId == selectedStationId) {
                Box(Modifier.fillMaxSize()) {
                    StationDetails(
                        uiState = uiState,
                        onRefresh = { viewModel.refreshStation() },
                        onToggleState = { viewModel.toggleStationState() },
                        onToggleMode = { viewModel.toggleControlMode() }
                    )
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            } else if (uiState.stationId == selectedStationId && !uiState.isLoading && uiState.errorMessage == null) {
                StationDetails(
                    uiState = uiState,
                    onRefresh = { viewModel.refreshStation() },
                    onToggleState = { viewModel.toggleStationState() },
                    onToggleMode = { viewModel.toggleControlMode() }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading station data...")
                    CircularProgressIndicator()
                }
            }
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
        // Header
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
                DetailRow(title = "Last Updated", value = uiState.lastUpdated)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Controls",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

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