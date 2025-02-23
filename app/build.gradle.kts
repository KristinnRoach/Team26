plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    // alias(libs.plugins.room)
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
    sourceCompatibility = JavaVersion.VERSION_11 // should be 17 ?
    targetCompatibility = JavaVersion.VERSION_11 // should be 17 ?
    }

    kotlinOptions {
        jvmTarget = "11" // should be 17 ?
    }

    buildFeatures {
        compose = true
        // buildConfig = true
    }
    /*
    room {
        schemaDirectory("$projectDir/schemas")
    }

     */

        composeOptions {
            kotlinCompilerExtensionVersion = "2.1.10"
        }



        packaging {
        resources {
            // Exclude the file that was causing build conflict
            excludes += "/META-INF/gradle/incremental.annotation.processors"

            // Recommended excludes for Dagger/Hilt projects
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"

            // This is important for Dagger/Hilt
            pickFirsts += "META-INF/gradle/incremental.annotation.processors"

        }
    }

}

dependencies {
// implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.lifecycle.runtimeCompose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)


    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.okhttp.logging)
    implementation(libs.accompanist.permissions)
    implementation(libs.hilt.android)
    implementation(libs.hilt.android.testing)
    implementation(libs.hilt.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler) {
        // exclude(group = "androidx.room", module = "room-compiler")
        exclude(group = "com.intellij", module = "annotations")
    }
// implementation(libs.hilt.core)
/*    implementation(libs.androidx.ui)
implementation(libs.androidx.ui.graphics)
implementation(libs.androidx.ui.tooling.preview)
implementation(libs.androidx.material3)
implementation(libs.androidx.lifecycle.runtime.ktx)


testImplementation(libs.junit)
androidTestImplementation(libs.androidx.junit)
androidTestImplementation(libs.androidx.espresso.core)
*/
    androidTestImplementation(platform(libs.androidx.compose.bom))
/*
androidTestImplementation(libs.androidx.ui.test.junit4)
debugImplementation(libs.androidx.ui.tooling)
debugImplementation(libs.androidx.ui.test.manifest)

 */
}