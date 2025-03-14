package com.example.team26

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.team26.ui.MainScreen
import com.example.team26.viewModel.MainViewModel
import com.example.team26.viewModel.viewModelFactory


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(darkColorScheme()) {
                val viewModel = viewModel<MainViewModel>(
                    factory = viewModelFactory {
                        MainViewModel(
                            PilotApp.appModule.stationRepository,
                            PilotApp.appModule.authRepository
                        )
                    }
                )
                MainScreen() // viewModel
            }
        }
    }
}