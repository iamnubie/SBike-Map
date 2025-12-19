import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.sbikemap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sbikemap"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "OPENAI_API_KEY", "\"${localProperties["OPENAI_API_KEY"]}\"")
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
        buildConfig = true
        compose = true
        viewBinding = true
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
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.navigation:navigation-compose:2.9.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.mapbox.maps:android-ndk27:11.17.1")
    implementation("com.mapbox.extension:maps-compose-ndk27:11.17.1")
    implementation(libs.accompanist.permissions)
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.appcompat:appcompat-resources:1.7.1")
    implementation("com.mapbox.navigationcore:android-ndk27:3.17.1")  // Adds core Navigation SDK functionality
    implementation("com.mapbox.navigationcore:copilot-ndk27:3.17.1")
    implementation("com.mapbox.navigationcore:ui-maps-ndk27:3.17.1")
    implementation("com.mapbox.navigationcore:voice-ndk27:3.17.1")
    implementation("com.mapbox.navigationcore:tripdata-ndk27:3.17.1")
    implementation("com.mapbox.navigationcore:android-ndk27:3.17.1")
    implementation("com.mapbox.navigationcore:ui-components-ndk27:3.17.1")
    implementation("com.mapbox.search:autofill-ndk27:2.17.1")
    implementation("com.mapbox.search:discover-ndk27:2.17.1")
    implementation("com.mapbox.search:place-autocomplete-ndk27:2.17.1")
    implementation("com.mapbox.search:offline-ndk27:2.17.1")
    implementation("com.mapbox.search:mapbox-search-android-ndk27:2.17.1")
    implementation("com.mapbox.search:mapbox-search-android-ui-ndk27:2.17.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-turf:7.9.0")

}