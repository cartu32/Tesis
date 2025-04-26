plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")

}
val roomVersion = "2.6.1" // Verifica que esta versión esté disponible en los repositorios

android {
    namespace = "com.example.comunicationwearmobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.AbuMonitor"
        minSdk = 31
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
    implementation(libs.androidx.activity)
    implementation(libs.androidx.lifecycle.viewmodel.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.datastore.core.android)
    implementation (libs.androidx.datastore.preferences)
    implementation (libs.play.services.maps)
    implementation (libs.gms.play.services.location)
    implementation (libs.android.maps.utils)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation (libs.gms.play.services.location)

    // Room

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Extensiones Kotlin para Room
    ksp("androidx.room:room-compiler:$roomVersion") // Procesador de anotaciones para Room

    //Lifecycle
    implementation ("androidx.activity:activity-ktx:1.9.3")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")


    //dependencias  agregadas para wear os
    implementation (libs.play.services.wearable)
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.x")
    implementation (libs.androidx.lifecycle.extensions)
    implementation (libs.androidx.lifecycle.runtime.ktx)
    implementation (libs.androidx.appcompat)
    implementation(libs.ui.tooling.preview)
    implementation (libs.gson)

    implementation (libs.kotlin.stdlib)
    implementation (libs.androidx.core.ktx.v160)
    implementation (libs.jetbrains.kotlin.parcelize.runtime)
    debugImplementation (libs.leakcanary.android)
    releaseImplementation (libs.leakcanary.android.no.op)
    //dependencias de librerias compartidas entre wear y mobile
    implementation(project(":shared_library"))
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
