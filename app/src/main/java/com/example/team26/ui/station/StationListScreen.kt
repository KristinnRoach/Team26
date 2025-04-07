package com.example.team26.ui.station

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer // Import Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height // Import height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults // Import CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// Enums remain the same
enum class FilterField(val displayName: String) {
    NAME("Name"),
    STATUS("Status"),
    CONTROL_MODE("Control Mode"),
    LAST_UPDATED("Last Updated")
}

enum class SortOrder(val displayName: String) {
    ASCENDING("Ascending"),
    DESCENDING("Descending")
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun StationListScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: StationViewModel,
) {
    val searchQuery = remember { mutableStateOf("") }
    val filterField = remember { mutableStateOf(FilterField.NAME) }
    val sortOrder = remember { mutableStateOf(SortOrder.ASCENDING) }

    // Observe states using collectAsState
    val stations by viewModel.allStationsUiState.collectAsState()
    val isApiReachable by viewModel.isApiReachable.collectAsState()
    val selectedStationId by viewModel.selectedStationId.collectAsState() // <<< COLLECT STATE HERE

    // Filtering logic remains the same
    val filteredStations = remember(stations, searchQuery.value, filterField.value, sortOrder.value) {
        stations
            .filter { it.stationName.contains(searchQuery.value, ignoreCase = true) }
            .sortedBy {
                when (filterField.value) {
                    FilterField.NAME -> it.stationName
                    FilterField.STATUS -> it.status
                    FilterField.CONTROL_MODE -> it.controlMode
                    FilterField.LAST_UPDATED -> it.lastUpdated
                }
            }.let {
                if (sortOrder.value == SortOrder.DESCENDING) it.reversed() else it
            }
    }

    // --- Conditional Rendering: List/Search OR Details ---
    if (selectedStationId == null) {
        // --- STATE 1: Show List and Search Bar ---
        Column(modifier = modifier.fillMaxSize()) { // Use the modifier passed to the screen
            if (!isApiReachable) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "API is currently unreachable.\nUnable to load or update station list.",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                // API is reachable, show search and list/empty state
                SearchAndFilterBar(
                    searchQuery = searchQuery.value,
                    onSearchQueryChange = { searchQuery.value = it },
                    filterField = filterField.value,
                    onFilterFieldChange = { filterField.value = it },
                    sortOrder = sortOrder.value,
                    onSortOrderChange = { sortOrder.value = it }
                )

                if (filteredStations.isEmpty()) { // Simplified empty check
                    val emptyText = if (searchQuery.value.isBlank()) "No stations found or still loading..." else "No stations match your search."
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emptyText, textAlign = TextAlign.Center)
                    }
                }
                else {
                    // Pass selectedStationId as null because we are in the list view
                    StationList(
                        stations = filteredStations,
                        selectedStationId = null, // Explicitly null here
                        modifier = Modifier.weight(1f),
                        onStationClick = { stationId -> // Renamed callback
                            viewModel.setStationId(stationId) // Set the ID to trigger detail view
                        }
                    )
                }
            }
        }
    } else {
        // --- STATE 2: Show Detail View ---
        // We re-use the existing StationScreen composable.
        // It internally observes uiState based on the selectedStationId.
        // The back navigation is handled by the TopAppBar added in MainScreen.
        StationScreen(
            modifier = modifier, // Use the modifier passed to the screen
            navController = navController, // Pass if needed by StationScreen internals
            viewModel = viewModel
            // The back action is now handled by the TopAppBar in MainScreen
            // which calls viewModel.setStationId(null)
        )
    }
}

@Composable
fun StationList(
    stations: List<StationDetailUiState>,
    selectedStationId: String?, // Still needed for potential future highlighting
    modifier: Modifier = Modifier,
    onStationClick: (String) -> Unit // Changed lambda name for clarity
) {
    LazyColumn(modifier = modifier) {
        items(stations, key = { it.stationId }) { station ->
            if (station.errorMessage != null && station.stationId.isEmpty()) {
                // Handle error item if necessary
            } else {
                val isSelected = selectedStationId == station.stationId // Keep for potential future use
                StationItem(
                    station = station,
                    isSelected = isSelected, // Pass the calculated boolean
                    onSelectStation = { // Renamed parameter in StationItem
                        onStationClick(station.stationId) // Call the passed lambda
                    }
                )
            }
        }
    }
}

// StationItem remains the same - it receives a Boolean for isSelected
@Composable
fun StationItem(
    station: StationDetailUiState,
    isSelected: Boolean, // Receives the calculated boolean
    modifier: Modifier = Modifier,
    onSelectStation: () -> Unit
) {
    // Optional: Use isSelected for highlighting
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onSelectStation() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor), // Apply highlight
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        when (station.status.uppercase()) {
                            "ON" -> Color(0xFF4CAF50)
                            "OFF" -> Color(0xFFF44336)
                            "ERROR" -> Color.Yellow
                            else -> Color.Gray
                        }
                    )
            )

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.stationName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mode: ${station.controlMode}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Last Update: ${station.lastUpdated}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}