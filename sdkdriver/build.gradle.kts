plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.moyasar.android.sdkdriver"
    compileSdk = 34
    buildToolsVersion = "34.0.0"

    defaultConfig {
        applicationId = "com.moyasar.android.sdkdriver"
        minSdk = 21
        targetSdk = 34

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    //noinspection GradleCompatible
    implementation("com.android.support:design:28.0.0")
    //noinspection GradleCompatible
    implementation("com.android.support:support-fragment:28.0.0")
    implementation("android.arch.lifecycle:extensions:1.1.1")
    implementation("com.android.support.constraint:constraint-layout:2.0.4")
    implementation(project(":sdk"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}