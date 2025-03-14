package com.example.team26.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object LaunchSettings {
    fun launchSettings(context: Context) {
        // Opens the device's app settings screen for your application
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}