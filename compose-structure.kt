// app/src/main/java/is/hi/hbv601g/pilotbloks

// Activities
package is.hi.hbv601g.pilotbloks.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PilotBloksApp()
            }
        }
    }
}

class StationControlActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stationId = intent.getStringExtra(EXTRA_STATION_ID) ?: return finish()
        setContent {
            MaterialTheme {
                StationControlScreen(stationId = stationId)
            }
        }
    }

    companion object {
        const val EXTRA_STATION_ID = "extra_station_id"
    }
}

// Composables
package is.hi.hbv601g.pilotbloks.ui.screens

@Composable
fun PilotBloksApp(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = { PilotBloksBottomNavigation(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "stations",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("stations") { StationListScreen() }
            composable("map") { MapScreen() }
            composable("station/{stationId}") { backStackEntry ->
                val stationId = backStackEntry.arguments?.getString("stationId")
                StationDetailScreen(stationId = stationId)
            }
        }
    }
}

@Composable
fun StationListScreen(
    viewModel: StationViewModel = viewModel()
) {
    val stations by viewModel.stations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column {
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged
        )
        LazyColumn {
            items(stations) { station ->
                StationListItem(station = station)
            }
        }
    }
}

@Composable
fun MapScreen(
    viewModel: StationViewModel = viewModel()
) {
    val stations by viewModel.stations.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(userLocation, 12f)
        }
    ) {
        stations.forEach { station ->
            Marker(
                state = MarkerState(station.location),
                title = station.name
            )
        }
    }
}

@Composable
fun StationControlScreen(
    stationId: String,
    viewModel: StationViewModel = viewModel()
) {
    val station by viewModel.selectedStation.collectAsState()

    Column {
        StationHeader(station = station)
        ControlPanel(
            state = station.state,
            mode = station.mode,
            onStateChange = viewModel::updateStationState,
            onModeChange = viewModel::updateStationMode
        )
    }
}

// Service
package is.hi.hbv601g.pilotbloks.services

class StatusMonitorService : LifecycleService() {
    private val viewModel: StationViewModel by viewModel()
    
    override fun onCreate() {
        super.onCreate()
        startMonitoring()
    }

    private fun startMonitoring() {
        lifecycleScope.launch {
            viewModel.stations.collect { stations ->
                checkStationsStatus(stations)
            }
        }
    }

    private fun checkStationsStatus(stations: List<Station>) {
        stations.forEach { station ->
            if (!station.isOnline) {
                createNotification(station)
            }
        }
    }
}

// ViewModel
package is.hi.hbv601g.pilotbloks.viewmodels

class StationViewModel(
    private val repository: StationRepository
) : ViewModel() {
    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations = _stations.asStateFlow()

    private val _selectedStation = MutableStateFlow<Station?>(null)
    val selectedStation = _selectedStation.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _userLocation = MutableStateFlow(LatLng(0.0, 0.0))
    val userLocation = _userLocation.asStateFlow()

    init {
        loadStations()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterStations()
    }

    private fun filterStations() {
        viewModelScope.launch {
            _stations.value = repository.searchStations(searchQuery.value)
        }
    }

    fun updateStationState(stationId: String, state: DeviceState) {
        viewModelScope.launch {
            repository.updateStationState(stationId, state)
            refreshStation(stationId)
        }
    }

    fun updateUserLocation(location: LatLng) {
        _userLocation.value = location
    }
}

// Repository and Data classes remain largely the same
package is.hi.hbv601g.pilotbloks.data.repository

class StationRepository(
    private val apiService: StationApiService,
    private val stationDao: StationDao
) {
    fun getStations(): Flow<List<Station>> = flow {
        try {
            val apiResponse = apiService.getStations()
            stationDao.insertAll(apiResponse)
            emit(apiResponse)
        } catch (e: Exception) {
            emit(stationDao.getAllStations())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun searchStations(query: String): List<Station> {
        return stationDao.searchStations("%$query%")
    }
}

// build.gradle (:app)
dependencies {
    implementation "androidx.activity:activity-compose:1.8.2"
    implementation platform('androidx.compose:compose-bom:2024.02.00')
    implementation "androidx.compose.ui:ui"
    implementation "androidx.compose.ui:ui-graphics"
    implementation "androidx.compose.ui:ui-tooling-preview"
    implementation "androidx.compose.material3:material3"
    
    // Navigation
    implementation "androidx.navigation:navigation-compose:2.7.7"
    
    // Maps
    implementation "com.google.maps.android:maps-compose:4.3.0"
    implementation "com.google.android.gms:play-services-maps:18.2.0"
    
    // Architecture Components
    implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"
    implementation "androidx.lifecycle:lifecycle-runtime-compose:2.7.0"
    implementation "androidx.room:room-ktx:2.6.1"
    
    // Networking
    implementation "com.squareup.retrofit2:retrofit:2.9.0"
    implementation "com.squareup.retrofit2:converter-gson:2.9.0"
    
    // Testing
    androidTestImplementation platform('androidx.compose:compose-bom:2024.02.00')
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
}