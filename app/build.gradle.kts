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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Debugging deprecations
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-Xlint:deprecation")
    }

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
            resourceConfigurations("en", "xxhdpi")
        }
    }
    */

    secrets {
        propertiesFileName = "secrets.properties"
        defaultPropertiesFileName = "local.defaults.properties"
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/*.kotlin_module"
            )
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
    implementation(libs.androidx.work)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.navigation.compose)

    // Crypto
    implementation(libs.androidx.security.crypto)

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

    // Location
    implementation(libs.play.services.location)

    // Maps
    implementation(libs.maplibre.compose)
    implementation(libs.maplibre.composeMaterial3)
//    implementation(libs.maplibre.android.sdk)
//    implementation(libs.maplibre.composeExtensions)
//    implementation(libs.maplibre.composeGeojson)
//    implementation(libs.maplibre.composeExperimental)
//    implementation(libs.spatialk.geojson)

    // Testing
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.compose.material.icons.extended)

    // Android Tests
    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
}