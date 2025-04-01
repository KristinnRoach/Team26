# pilot-bloks - Android App

An Android application prototype designed for monitoring and managing the "pilot-bloks" street lighting system in Reykjavik.

## ✨ Key Features

*   📱 View list of street light stations and their status (ON/OFF/ERROR).
*   🔧 View detailed information and location for each station.
*   ⚙️ Control station state (ON/OFF) and mode (ASTRO/MANUAL) - subject to API rules.
*   🗺️ Visualize stations on an interactive map.
*   🔔 Receive background notifications for important events (errors, recovery, connectivity) based on configurable preferences.

## 🛠️ Technology Stack

*   **Kotlin:** Primary language.
*   **Jetpack Compose:** Modern declarative UI toolkit.
*   **ViewModel:** Lifecycle-aware state management.
*   **Room:** Local persistence for notifications and cached states.
*   **Retrofit:** Type-safe HTTP client for API communication.
*   **WorkManager:** Reliable background task execution (status checking).
*   **MapLibre Compose:** Interactive map display.
*   **Coroutines & Flow:** Asynchronous programming.

## 🚨 API Emulator Required

This application needs a backend API to function. A Python/Flask based emulator is included in the `/apiEmulator` directory.

**Please follow the instructions in `/apiEmulator/README.md` to set it up and run it *before* launching the Android app.**

## 📋 Development Requirements

*   ava Development Kit (JDK) 17 or higher.
*   Android Studio Flamingo (2022.2.1) or newer.
*   Minimum Android API level: 29 (Android 10).

## ▶️ Getting Started

1.  Clone this repository: `git clone <your-repo-url>`
2.  **Set up and run the API Emulator** (see instructions in `/apiEmulator/README.md`). *This is essential.*
3.  Open the project root directory in Android Studio.
4.  Sync Gradle dependencies.
5.  Build and run the app on an Android Emulator (API 29+) or a physical device. Ensure the device/emulator can reach the API emulator address (usually `http://10.0.2.2:5053` for Android Emulator talking to host machine).