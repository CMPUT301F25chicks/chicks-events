plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.chicksevent"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.chicksevent"
        minSdk = 24
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.firebase:firebase-database:20.3.1")
    androidTestImplementation("com.google.firebase:firebase-database:20.3.1")



    // Mockito core library
    testImplementation("org.mockito:mockito-inline:5.2.0")

    testImplementation("org.mockito:mockito-android:5.13.0")

    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Optional: if you use Kotlin-specific Mockito support
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    // Optional: if you test Android components with Mockito
    androidTestImplementation("org.mockito:mockito-android:5.13.0")
//    implementation(files("C:\\Users\\Dion Alex Mathew\\AppData\\Local\\Android\\Sdk\\platforms\\android-36\\android.jar"));
}

