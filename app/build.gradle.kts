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
    implementation("androidx.viewpager2:viewpager2:1.1.0")
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(libs.core.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}