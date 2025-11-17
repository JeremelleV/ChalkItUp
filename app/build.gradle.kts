plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.chalkitup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.chalkitup"
        minSdk = 33 //Minimum android version
        targetSdk = 35 //latest tested version
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
        // Enable Java 8+ API desugaring for minSdk < 26
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packagingOptions {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
        }
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
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.storage)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.runtime.saved.instance.state)
    testImplementation(libs.testng)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose.v275)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    implementation(libs.coil.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.foundation.v151) // New
    implementation(libs.compose) // Calendar
    implementation(libs.firebase.firestore.ktx)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    coreLibraryDesugaring(libs.desugar.jdk.libs.v204)
    implementation(libs.google.firebase.firestore.ktx)
    implementation(libs.androidx.material)
    implementation(libs.places)
    implementation(libs.places.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.lifecycle.runtime.ktx.v262)
    implementation(libs.firebase.database.ktx) // Check for latest version
    implementation(libs.androidx.datastore)
    implementation(libs.gson)
    implementation(libs.firebase.database.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.lifecycle.runtime.ktx.v262)
    implementation(libs.androidx.runtime.livedata.v150)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth.v2070)
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.appcompat.v120)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.emoji2.bundled)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.auth.v2100)
    implementation(libs.play.services.auth)
    implementation(libs.protobuf.javalite)
    // Google Calendar API dependencies
    implementation(libs.google.api.client)  // Google API Client
    implementation(libs.google.oauth.client)  // OAuth for authentication
    implementation(libs.google.api.services.calendar)  // Google Calendar API
    // JSON parsing support for Google APIs
    implementation(libs.google.http.client.gson)
    // Gif display
    implementation(libs.glide)
    implementation(libs.glide.compose)
    implementation(libs.accompanist.flowlayout)
    //Chatbot AI needed dependencies
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    // Hilt
    implementation (libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.com.google.firebase.firebase.firestore.ktx)
    // Unit testing
    testImplementation (libs.junit)
    testImplementation (libs.kotlinx.coroutines.test.v164)
    // Mocking
    testImplementation (libs.mockito.core)
    testImplementation (libs.mockito.kotlin)
    // Truth assertions
    testImplementation (libs.truth)
    testImplementation (libs.androidx.core.testing)
    testImplementation (libs.mockito.core.v5170)
    testImplementation (libs.mockito.core.v4xx)
    testImplementation(libs.mockk.v11310)
    testImplementation(libs.mockito.inline.v4110)
    testImplementation(libs.kotlinx.coroutines.test.v173)
    // For LiveData testing
    testImplementation(libs.turbine)
    implementation (libs.kotlinx.coroutines.play.services)
    testImplementation (libs.robolectric)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}