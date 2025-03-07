package com.example.team26

import android.app.Application
import com.example.team26.shared.di.AppModule
import com.example.team26.shared.di.AppModuleImpl

class PilotApp : Application() {
    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)
    }
}