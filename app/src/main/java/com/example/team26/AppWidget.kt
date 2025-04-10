package com.example.team26

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AppWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, AppWidget::class.java))

        when (intent.action) {
            "WIDGET_ON" -> {
                postToEmulator("http://10.0.2.2:5053/devices/dev2/state", """{"state": "ON"}""")
            }
            "WIDGET_OFF" -> {
                postToEmulator("http://10.0.2.2:5053/devices/dev2/state", """{"state": "OFF"}""")
            }
            "WIDGET_TOGGLE_ASTRO" -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = URL("http://10.0.2.2:5053/devices/dev2").readText()
                        val currentAstro = JSONObject(response).getString("astroman")
                        val newMode = if (currentAstro.uppercase() == "ASTRO") "MANUAL" else "ASTRO"
                        postToEmulator(
                            "http://10.0.2.2:5053/devices/dev2/astroman",
                            """{"astroman": "$newMode"}"""
                        )
                    } catch (e: Exception) {
                        Log.e("WIDGET", "Failed to toggle ASTRO mode", e)
                    }
                }
            }
        }

        // Delay to allow backend changes to take effect
        CoroutineScope(Dispatchers.IO).launch {
            delay(800)
            ids.forEach { updateAppWidget(context, manager, it) }
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.app_widget)

    setButtonActions(context, views)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val connection = URL("http://10.0.2.2:5053/devices/dev2").openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)

            var name = json.optString("name", "")
            if (name.isEmpty()) {
                name = "Station dev2"
            }
            val state = json.optString("outputstate", json.optString("uxstate", "N/A"))
            val mode = json.optString("astroman", "?").uppercase()

            // Set text color based on mode and state only in MANUAL mode
            val textColor = when {
                mode == "MANUAL" && state.uppercase() == "ON" -> Color.GREEN
                mode == "MANUAL" && state.uppercase() == "OFF" -> Color.RED
                else -> Color.DKGRAY  // For ASTRO or unexpected state
            }

            val displayText = "Status: $state ($mode)"

            // Disable the ON/OFF buttons when not in MANUAL mode
            val isManual = mode == "MANUAL"
            views.setBoolean(R.id.widget_button_on, "setEnabled", isManual)
            views.setBoolean(R.id.widget_button_off, "setEnabled", isManual)

            // Update the toggle button label
            val toggleLabel = if (isManual) "Switch to ASTRO" else "Switch to MANUAL"
            views.setTextViewText(R.id.widget_button_toggle, toggleLabel)

            Log.d("WIDGET", "Updating widget: name=$name, $displayText")

            withContext(Dispatchers.Main) {
                views.setTextViewText(R.id.widget_station_name, name)
                views.setTextViewText(R.id.widget_station_status, displayText)
                views.setTextColor(R.id.widget_station_status, textColor)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        } catch (e: Exception) {
            Log.e("WIDGET", "Failed to load station data", e)
        }
    }

    // Early update push to avoid a blank widget while waiting
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun setButtonActions(context: Context, views: RemoteViews) {
    // We now have three actions corresponding to the two rows:
    // ON, OFF (in the first row) and the mode toggle (in the second row)
    val actions = listOf(
        Triple("WIDGET_ON", R.id.widget_button_on, 0),
        Triple("WIDGET_OFF", R.id.widget_button_off, 1),
        Triple("WIDGET_TOGGLE_ASTRO", R.id.widget_button_toggle, 2)
    )

    for ((action, buttonId, requestCode) in actions) {
        val intent = Intent(context, AppWidget::class.java).apply { this.action = action }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(buttonId, pendingIntent)
    }
}

private fun postToEmulator(url: String, jsonBody: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            if (jsonBody.isNotEmpty()) {
                connection.outputStream.use { it.write(jsonBody.toByteArray()) }
            }
            Log.d("WIDGET", "POST to $url successful (${connection.responseCode})")
            connection.disconnect()
        } catch (e: Exception) {
            Log.e("WIDGET", "POST to $url failed", e)
        }
    }
}