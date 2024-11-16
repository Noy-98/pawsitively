plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.pawsitively"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pawsitively"
        minSdk = 25
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
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15") // Use the latest version
    }
}

dependencies {
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    dependencies {
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation(libs.recyclerview)
        implementation("com.google.firebase:firebase-database-ktx:20.3.0") // Keep this
        implementation("com.google.firebase:firebase-storage-ktx:20.3.0") // Keep this
        implementation("com.github.bumptech.glide:glide:4.12.0")
        implementation ("de.hdodenhof:circleimageview:3.1.0")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("androidx.core:core-ktx:1.12.0")

        // Testing dependencies
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
    }

}