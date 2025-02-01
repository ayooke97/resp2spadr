plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.aicourse"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.aicourse"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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
    flavorDimensions += "api"
    
    productFlavors {
        create("api24") {
            dimension = "api"
            minSdk = 24
            targetSdk = 24
            versionNameSuffix = "-API24"
        }
        
        create("api29") {
            dimension = "api"
            minSdk = 29
            targetSdk = 29
            versionNameSuffix = "-API29"
        }
        
        create("api31") {
            dimension = "api"
            minSdk = 31
            targetSdk = 31
            versionNameSuffix = "-API31"
        }
        
        create("api33") {
            dimension = "api"
            minSdk = 33
            targetSdk = 33
            versionNameSuffix = "-API33"
        }
        
        create("api34") {
            dimension = "api"
            minSdk = 34
            targetSdk = 34
            versionNameSuffix = "-API34"
        }
        
        create("api35") {
            dimension = "api"
            minSdk = 35
            targetSdk = 35
            versionNameSuffix = "-API35"
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.glide)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    kapt(libs.glide.compiler)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}