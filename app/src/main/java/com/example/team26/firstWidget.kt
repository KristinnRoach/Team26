package com.example.team26

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.team26.data.station.MockStationRepositoryImpl
import com.example.team26.domain.station.DeviceState
import android.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.ComponentName
import com.example.team26.data.station.MockStationRepository
import com.example.team26.domain.station.ControlMode

class FirstWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
        Log.d("WIDGET", "onUpdate called for widgetId(s): ${appWidgetIds.joinToString()}")
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        CoroutineScope(Dispatchers.Default).launch {
            val repo = MockStationRepository
            when (intent.action) {
                "WIDGET_ON" -> {
                    Log.d("WIDGET", "ON button clicked")
                    repo.updateStationState("station-1", DeviceState.ON)
                }
                "WIDGET_OFF" -> {
                    Log.d("WIDGET", "OFF button clicked")
                    repo.updateStationState("station-1", DeviceState.OFF)
                }
                "WIDGET_TOGGLE_ASTRO" -> {
                    Log.d("WIDGET", "TOGGLE ASTRO clicked")
                    val station = repo.getStationSnapshot("station-1")
                    val newMode = if (station.controlMode == ControlMode.ASTRO)
                        ControlMode.MANUAL else ControlMode.ASTRO
                    repo.updateControlMode("station-1", newMode)
                }
            }

            // Refresh widget after update
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, FirstWidget::class.java))
            for (id in ids) {
                updateWidget(context, appWidgetManager, id)
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.first_widget)

        // data
        val station = MockStationRepository.getStationSnapshot("station-1")
        views.setTextViewText(R.id.widget_station_name, station.name)
        views.setTextViewText(R.id.widget_station_status, "Status: ${station.state}")

        //color on state
        val color = when (station.state) {
            DeviceState.ON -> Color.GREEN
            DeviceState.OFF -> Color.RED
            DeviceState.ERROR -> Color.GRAY
        }
        views.setTextColor(R.id.widget_station_status, color)

        // Button Intents
        val intentOn = Intent(context, FirstWidget::class.java).apply { action = "WIDGET_ON" }
        val pendingIntentOn = PendingIntent.getBroadcast(context, 0, intentOn, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_button_on, pendingIntentOn)

        val intentOff = Intent(context, FirstWidget::class.java).apply { action = "WIDGET_OFF" }
        val pendingIntentOff = PendingIntent.getBroadcast(context, 1, intentOff, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_button_off, pendingIntentOff)

        val intentToggle = Intent(context, FirstWidget::class.java).apply { action = "WIDGET_TOGGLE_ASTRO" }
        val pendingIntentToggle = PendingIntent.getBroadcast(context, 2, intentToggle, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_button_toggle, pendingIntentToggle)

        appWidgetManager.updateAppWidget(widgetId, views)
    }
}