plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "cloud.pace.sdk.app"
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        applicationId = Config.APPLICATION_ID
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = Libs.TEST_INSTRUMENTATION_RUNNER

        manifestPlaceholders["pace_redirect_scheme"] = "pace.99b69996-9d26-4d73-8dd2-b4414f2c8826"
        manifestPlaceholders["appAuthRedirectScheme"] = "cloud-sdk-example"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "version"

    productFlavors {
        create("default") {
            dimension = "version"
            buildConfigField("boolean", "TOKEN_EXCHANGE", "false")
        }

        create("tokenExchange") {
            dimension = "version"
            buildConfigField("boolean", "TOKEN_EXCHANGE", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.JETPACK_COMPOSE_COMPILER
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":library"))

    // Android
    implementation(Libs.CORE_KTX)
    implementation(Libs.APPCOMPAT)
    implementation(Libs.CONSTRAINT_LAYOUT)
    implementation(Libs.RECYCLER_VIEW)
    implementation(Libs.LIFECYCLE_RUNTIME_KTX)
    implementation(Libs.LIFECYCLE_LIVE_DATA_KTX)
    implementation(Libs.NAVIGATION_COMPOSE)
    implementation(Libs.NAVIGATION_FRAGMENT)
    implementation(Libs.NAVIGATION_UI)

    // Jetpack Compose
    implementation(Libs.JETPACK_COMPOSE_MATERIAL)
    implementation(Libs.JETPACK_COMPOSE_MATERIAL_ICONS)
    implementation(Libs.JETPACK_COMPOSE_RUNTIME_LIVEDATA)
    implementation(Libs.JETPACK_COMPOSE_UI_TOOLING_PREVIEW)
    debugImplementation(Libs.JETPACK_COMPOSE_UI_TOOLING)
    implementation(Libs.JETPACK_COMPOSE_CONSTRAINT_LAYOUT)
}
