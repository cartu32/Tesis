plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.comunicationwearmobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.NotificationViewWearMobile"
        minSdk = 30
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.compose.material)
    implementation (libs.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.wearable)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //dependencias  agregadas para wear os
    implementation ("com.google.android.gms:play-services-wearable:18.1.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.7")

    //dependencias de librerias compartidas entre wear y mobile
    implementation(project(":shared_library"))
    wearApp(project(":wear"))
}