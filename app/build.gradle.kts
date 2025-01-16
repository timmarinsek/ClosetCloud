plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cloudcloset"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cloudcloset"
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // ---------- Version Catalog Dependencies ----------
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.core.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ---------- Other Libraries ----------
    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // ---------- CameraX Dependencies ----------
    val cameraXVersion = "1.3.0"
    implementation("androidx.camera:camera-core:$cameraXVersion")
    implementation("androidx.camera:camera-camera2:$cameraXVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraXVersion")
    // Optional: for the camera preview widget
    implementation("androidx.camera:camera-view:$cameraXVersion")
    // Optional: for advanced camera effects (HDR, etc.)
    implementation("androidx.camera:camera-extensions:$cameraXVersion")
}
