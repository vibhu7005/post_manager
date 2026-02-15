plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.demoapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.demoapplication"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    
    // ViewModel & LiveData
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.localbroadcastmanager)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // For viewModelScope
    // lifecycle-runtime-ktx already included above for lifecycleScope
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation(libs.litert.api)
    
    // Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // SSE (Server-Sent Events)
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    
    // Enhanced Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Unit Testing
    testImplementation(libs.junit)
    
    // MockK - Mocking library for Kotlin
    testImplementation("io.mockk:mockk:1.13.8")
    
    // Coroutines Test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Turbine - Flow testing (optional, for Flow tests)
    testImplementation("app.cash.turbine:turbine:1.0.0")
    
    // Truth - Better assertions (optional)
    testImplementation("com.google.truth:truth:1.1.4")
    
    // AndroidX Test (for ViewModel testing)
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    // Android Instrumented Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}