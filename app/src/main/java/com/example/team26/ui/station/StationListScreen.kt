import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.team26.PilotApp
import com.example.team26.ui.station.SearchAndFilterBar
import com.example.team26.ui.station.StationDetailUiState
import com.example.team26.ui.station.StationViewModel
import com.example.team26.viewModel.viewModelFactory

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

@Composable
fun StationListScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: StationViewModel = viewModel(
        factory = viewModelFactory {
            StationViewModel(PilotApp.appModule.stationRepository)
        }
    )
) {
    val searchQuery = remember { mutableStateOf("") }
    val filterField = remember { mutableStateOf(FilterField.NAME) }
    val sortOrder = remember { mutableStateOf(SortOrder.ASCENDING) }
    val stations by viewModel.allStationsUiState.collectAsState()

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

    Column(modifier = modifier.fillMaxSize()) {
        SearchAndFilterBar(
            searchQuery = searchQuery.value,
            onSearchQueryChange = { searchQuery.value = it },
            filterField = filterField.value,
            onFilterFieldChange = { filterField.value = it },
            sortOrder = sortOrder.value,
            onSortOrderChange = { sortOrder.value = it }
        )
        StationList(stations = filteredStations)
    }
}

@Composable
fun StationList(stations: List<StationDetailUiState>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(stations, key = { it.stationId }) { station ->
            StationItem(station = station)
        }
    }
}

@Composable
fun StationItem(station: StationDetailUiState, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = station.stationName, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 8.dp)
                        .clip(CircleShape)
                        .background(
                            when (station.status.lowercase()) {
                                "on" -> Color.Green
                                "off" -> Color.Red
                                "error" -> Color.Transparent
                                else -> Color.Gray
                            }
                        )
                )
                // Status text
                Text(
                    text = if (station.status.lowercase() == "error")
                        "Status: Error"
                    else
                        "Status: ${station.status}"
                )
            }
            Text(text = "Control Mode: ${station.controlMode}")
            Text(text = "Last Updated: ${station.lastUpdated}")
        }
    }
}