package com.example.team26.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.team26.di.AppModule
import com.example.team26.domain.notification.NotificationPreference
import java.util.Locale

fun NotificationPreference.getDisplayName(): String {
    return when(this) {
        NotificationPreference.API_STATUS_CHANGE -> "API Status Change" // Specific name
        else -> this.name
            .replace('_', ' ')
            .lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

fun NotificationPreference.getDescription(): String {
    return when (this) {
        NotificationPreference.STATION_ERROR -> "Notify when a station reports an error."
        NotificationPreference.API_STATUS_CHANGE -> "Notify when the API becomes unreachable or recovers."
        NotificationPreference.STATE_CHANGE -> "Notify on normal ON/OFF state changes."
        NotificationPreference.MODE_CHANGE -> "Notify on normal ASTRO/MANUAL mode changes."
        NotificationPreference.STATION_ADDED_REMOVED -> "Notify when stations appear or disappear."
        NotificationPreference.STATION_RECOVERY -> "Notify when a station recovers from an error."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {

    // Managers
    val apiSettingsManager = remember { AppModule.apiSettingsManager }
    val preferenceManager = remember { AppModule.notificationPreferenceManager }

    // State for API Fields
    var apiUrl by remember { mutableStateOf(apiSettingsManager.getApiUrl()) }
    var clientId by remember { mutableStateOf(apiSettingsManager.getClientId()) }
    var clientSecret by remember { mutableStateOf(apiSettingsManager.getClientSecret()) }
    var passwordVisible by remember { mutableStateOf(false) }

    // State for Notification Preferences
    val notificationPreferencesState = remember { mutableStateOf(preferenceManager.getAllPreferences()) }

    // State for Restart Message
    var showRestartMessage by remember { mutableStateOf(false) }


    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // API Connection Section
            Text(
                text = "API Connection",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = apiUrl,
                onValueChange = {
                    apiUrl = it
                    showRestartMessage = false
                },
                label = { Text("API Endpoint URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = clientId,
                onValueChange = {
                    clientId = it
                    showRestartMessage = false
                },
                label = { Text("Client ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = clientSecret,
                onValueChange = {
                    clientSecret = it
                    showRestartMessage = false
                },
                label = { Text("Client Secret") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.VisibilityOff
                    else Icons.Filled.Visibility

                    val description = if (passwordVisible) "Hide secret" else "Show secret"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
            Spacer(Modifier.height(16.dp))

            // Save API Settings Button
            Button(
                onClick = {
                    apiSettingsManager.setApiUrl(apiUrl)
                    apiSettingsManager.setClientId(clientId)
                    apiSettingsManager.setClientSecret(clientSecret)

                    showRestartMessage = true
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save API Settings")
            }

            // Persistent Restart Message
            if (showRestartMessage) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Settings saved. Please restart the app for API changes to take effect.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }


            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Notification Preferences Section
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            NotificationPreference.entries.forEach { preference ->
                PreferenceItem(
                    title = preference.getDisplayName(),
                    description = preference.getDescription(),
                    checked = notificationPreferencesState.value[preference] ?: preference.defaultEnabled,
                    onCheckedChange = { isChecked ->
                        notificationPreferencesState.value = notificationPreferencesState.value + (preference to isChecked)
                        preferenceManager.setPreferenceEnabled(preference, isChecked)
                    }
                )
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun PreferenceItem(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}