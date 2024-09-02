plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
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
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //dependencias  agregadas para wear os
    implementation (libs.play.services.wearable)
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:")
    implementation (libs.androidx.lifecycle.extensions)
    implementation (libs.androidx.lifecycle.runtime.ktx)
    implementation (libs.androidx.appcompat)
    implementation(libs.ui.tooling.preview)
    implementation (libs.gson)

    implementation (libs.kotlin.stdlib)
    implementation (libs.androidx.core.ktx.v160)
    implementation (libs.jetbrains.kotlin.parcelize.runtime)

    //dependencias de librerias compartidas entre wear y mobile
    implementation(project(":shared_library"))
    wearApp(project(":wear"))
}