plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-android")
    id ("kotlin-parcelize")
    id ("kotlin-kapt")
    id("maven-publish")
}

android {
    namespace = "com.moyasar.android.sdk"
    compileSdk = 34
    buildToolsVersion = "34.0.0"
    version = "1.0.0"
    group = "com.github.AbdulazizAlrabiah"

    defaultConfig {
        minSdk = 17

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

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
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("android.arch.lifecycle:extensions:1.1.1")
    implementation("com.android.support.constraint:constraint-layout:2.0.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}