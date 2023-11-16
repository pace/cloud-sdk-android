import com.google.gson.Gson

val configFileReader = rootProject.file("config.json").reader()
val configJson: Config = Gson().fromJson(configFileReader, Config::class.java)

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.dagger.hilt.android")
    id("com.google.android.gms.oss-licenses-plugin")
    id("io.sentry.android.gradle") version "3.14.0"
}

android {
    namespace = "car.pace.cofu"
    compileSdk = 34

    signingConfigs {
        create("config") {
            keyAlias = configJson.signing.keyAlias
            keyPassword = configJson.signing.keyAliasPassword
            storeFile = file(configJson.signing.keyPath)
            storePassword = configJson.signing.keyPassword
        }
    }

    defaultConfig {
        applicationId = "car.pace.cofu"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CLOUD_API_KEY", "\"" + configJson.sdk.apiKey + "\"")
        buildConfigField("String", "PACE_CLIENT_ID", "\"" + configJson.sdk.clientId + "\"")
        buildConfigField("String", "PACE_REDIRECT_URL", "\"" + configJson.sdk.redirectUrl + "\"")

        // appAuthRedirectScheme is needed for AppAuth in IDKit and pace_redirect_scheme is needed for deep linking in AppKit
        manifestPlaceholders["appAuthRedirectScheme"] = configJson.sdk.redirectScheme // e.g. reverse domain name notation: cloud.pace.app
        manifestPlaceholders["pace_redirect_scheme"] = configJson.sdk.uniqueId // e.g. pace.ad50262a-9c88-4a5f-bc55-00dc31b81e5a

        resValue("string", "app_name", configJson.appName)

        resourceConfigurations += arrayOf("en", "cs", "de", "es", "fr", "it", "nl", "pl", "pt", "ro", "ru")

        // Setup crash reporting
        buildConfigField("Boolean", "SENTRY_ENABLED", configJson.sentry.enabled.toString())
        buildConfigField("String", "SENTRY_DSN", "\"" + configJson.sentry.dsn as? String + "\"")

        val crashlyticsEnabled = configJson.crashlyticsEnabled
        buildConfigField("Boolean", "FIREBASE_ENABLED", crashlyticsEnabled.toString())
        if (crashlyticsEnabled) {
            apply(plugin = "com.google.gms.google-services")
            apply(plugin = "com.google.firebase.crashlytics")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("config")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    lint {
        disable.addAll(listOf("LogNotTimber", "StringFormatInTimber", "ThrowableNotAtBeginning", "BinaryOperationInTimber", "TimberArgCount", "TimberArgTypes", "TimberTagLength"))
    }

    kapt {
        // Allow references to generated code
        correctErrorTypes = true
    }
}

dependencies {
    // PACE Cloud SDK
    implementation("cloud.pace:sdk:20.1.0")

    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0-rc01")
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Google support and design libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.1")

    // Fancy recyclerview animation
    implementation("jp.wasabeef:recyclerview-animators:4.0.2")

    // Hilt dependency injection
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-android-compiler:2.48.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    kaptTest("com.google.dagger:hilt-android-compiler:2.48.1")

    // Crash reporting
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
}
