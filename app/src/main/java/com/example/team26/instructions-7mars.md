
# Steps for Adding a New Feature

## First: Essential Steps
1. **Create a domain entity in `/domain/[feature]`** *(for data that needs persistence or sharing)*
    - Define your data model like `data class AlertEntity(val id: String, val message: String)` in `/domain/alert/AlertEntity.kt`.

2. **Define a repository interface in `/domain/[feature]`** *(for features requiring data access)*
    - Create an interface like `interface AlertRepository { fun getAlerts(): Flow<List<AlertEntity>> }` in `/domain/alert/AlertRepository.kt`.

3. **Create a repository implementation in `/data/[feature]`** *(implements data access logic)*
    - Implement the repository like `class AlertRepositoryImpl : AlertRepository { override fun getAlerts(): Flow<List<AlertEntity>> = ... }` in `/data/alert/AlertRepositoryImpl.kt`.

4. **Create a UI screen in `/ui/[feature]`**
    - Build your screen like `@Composable fun AlertScreen(viewModel: AlertViewModel = viewModel(factory = viewModelFactory { AlertViewModel(PilotApp.appModule.alertRepository) })` in `/ui/alert/AlertScreen.kt`.
    - *For simple screens, use local state:* `val items = remember { mutableStateOf(listOf<String>()) }`

5. **Add a navigation item in `/navigation/NavItem.kt`**
    - Create a new navigation item like `object Alert : NavItem("alert", "Alert", Icons.Default.Notifications)`.
    - 
6. **Add screen to nav graph in `MainScreen.kt`** *(if required for navigation)*
    - Add your route like `composable(NavItem.Alert.route) { AlertScreen() }` in `/ui/MainScreen.kt`.

## Next: Add if / when needed
7. **Add repository to AppModule interface in `/di/AppModule.kt`**
    - Add a property like `val alertRepository: AlertRepository`.

8. **Initialize repository in AppModuleImpl in `/di/AppModule.kt`**
    - Override with implementation like `override val alertRepository: AlertRepository by lazy { AlertRepositoryImpl() }`.

9. **Create a ViewModel in `/ui/[feature]`**
    - Define a ViewModel like `class AlertViewModel(private val repository: AlertRepository) : ViewModel()` in `/ui/alert/AlertViewModel.kt`.

10. **Create UI state class in your ViewModel file**
    - Add `data class AlertUiState(val alerts: List<AlertEntity> = emptyList(), val isLoading: Boolean = false)` in `AlertViewModel.kt`.

11. **If needed, create DAO in `/domain/[feature]`**
    - For database access, add `@Dao interface AlertDao { @Query("SELECT * FROM alerts") fun getAlerts(): Flow<List<AlertEntity>> }` in `/domain/alert/AlertDao.kt`.

12. **If using Room, update AppDb in `/data/AppDb.kt`**
    - Add entity and DAO like `@Database(entities = [..., AlertEntity::class])` and `abstract fun alertDao(): AlertDao`.

13. **For API endpoints, create API interface in `/data/remote`**
    - Define your service like `interface AlertApi { @GET("alerts") suspend fun getAlerts(): List<AlertDto> }` in `/data/remote/AlertApi.kt`.

14. **For complex types in Room, add TypeConverters in `/data/converters`**
    - Create converters like `class AlertTypeConverters { @TypeConverter fun convertToString(value: AlertType): String = value.name }` in `/data/converters/AlertTypeConverters.kt`.

15. **Update the UI components in `/design/components`**
    - Add any reusable components like `@Composable fun AlertItem(alert: AlertEntity)` in `/design/components/AlertItem.kt`.


# When to Create Domain Entity, Repository, ViewModel vs. Simpler Approaches

## Create Full Architecture (Entity + Repository + ViewModel) When:

- The feature needs to access or store data that will be used across multiple screens
- The feature requires data persistence (database storage)
- The feature involves network API calls
- The feature includes complex business logic that should be separated from the UI
- The feature needs to maintain state across configuration changes
- Multiple UI components need to observe the same data

## Simpler Approaches (Skip Full Architecture):

- For UI-only components that don't need persistent state (use local state with `remember` and `mutableStateOf`)
- For simple screens with minimal logic (create screen file only without a ViewModel)
- For UI components used in a single location (create within the parent screen file)
- For static data that doesn't change (define as constants in the relevant file)
- For navigation-related changes (just update the NavItem and navigation graph)
- For theme/style modifications (update directly in the design/theme directory)
- For small utility functions (add directly where needed or in a utils package)
- For UI state that doesn't need to survive process death (use `rememberSaveable` instead of a ViewModel)

## Rule of Thumb

Start simple and add architecture layers only when they provide clear benefits. If a feature can be implemented with local state in a composable function, start there and extract into the full architecture pattern when the complexity grows.
