package com.example.team26

import android.app.Application

class PilotApp : Application() {
    // No need for initializing appModule since we're using an object now
}


/*
// old DI version - ignore for now
import com.example.team26.di.AppModule
import com.example.team26.di.AppModuleImpl

companion object {
    lateinit var appModule: AppModule
}

override fun onCreate() {
    super.onCreate()
    appModule = AppModuleImpl(this)
}

 */