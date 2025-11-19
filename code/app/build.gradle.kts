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

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            // If you ever use Robolectric:
            // isIncludeAndroidResources = true
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
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.7")
    androidTestImplementation("org.mockito:mockito-android:5.10.0")
    // AndroidX Test Core library
    androidTestImplementation ("androidx.test:core:1.5.0") // Use the latest stable version

    // AndroidX Test Runner and Rules (JUnit 4)
    androidTestImplementation ("androidx.test:runner:1.5.2") // Use the latest stable version
    androidTestImplementation ("androidx.test:rules:1.5.0") // Use the latest stable version

    // Espresso for UI interaction testing
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1") // Use the latest stable version
    implementation("com.google.firebase:firebase-storage:20.2.1")
//

//    implementation(files("C:\\Users\\jorda\\AppData\\Local\\Android\\Sdk\\platforms\\android-36\\android.jar"));



// mock final classes like DataSnapshot
}


tasks.withType<Javadoc> {
    isFailOnError = false
}