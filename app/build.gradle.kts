plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.team26"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.team26"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // should be 17 ?
        targetCompatibility = JavaVersion.VERSION_17 // should be 17 ?
    }

    kotlinOptions {
        jvmTarget = "17" // should be 17 ?
        // freeCompilerArgs += listOf("-Xlint:deprecation")
    }

    // for debugging versions in generated build files
    tasks.withType<JavaCompile>().configureEach { options.compilerArgs.add("-Xlint:deprecation") }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    /*
    productFlavors { // TODO: fix or remove
        create("dev") {
            // The following configuration limits the "dev" flavor to using
            // English string resources and xxhdpi screen-density resources.
            resourceConfigurations("en", "xxhdpi")
        }
    }
     */

    /* Should this be here??
    composeOptions {
        kotlinCompilerExtensionVersion = "2.1.10" // incorrect version??
    }
    */

    secrets {
        // To add your Maps API key to this project:
        // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
        // 2. Add this line, where YOUR_API_KEY is your API key:
        //        MAPS_API_KEY=YOUR_API_KEY
        propertiesFileName = "secrets.properties"

        // A properties file containing default secret values. This file can be
        // checked in version control.
        defaultPropertiesFileName = "local.defaults.properties"
    }


    packaging {
        resources {
            // Essential META-INF excludes for Room and general Android projects
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*.kotlin_module"
            
            // Needed for Room annotation processor
            pickFirsts += "META-INF/gradle/incremental.annotation.processors"
        }
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Network
    implementation(libs.retrofit.core)
    implementation(libs.converter.gson)
    implementation(libs.okhttp.logging)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Maps
    implementation(libs.maplibre.compose)
    implementation(libs.maplibre.composeMaterial3)

    // implementation(libs.ramani.maplibre)

    // Just in case the free stuff is a hassle: Android Maps Compose composables for the Maps SDK for Android
    // implementation(libs.maps.compose)
    // implementation(libs.play.services.maps)


    // Testing
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)

    // Android Tests
    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
}