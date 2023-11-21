import car.pace.cofu.configuration.CONFIGURATION_FILE_NAME
import car.pace.cofu.configuration.Configuration
import car.pace.cofu.menu.MenuEntriesGenerator
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.google.gson.Gson
import java.util.UUID

private val menuEntriesTask = "generateMenuEntries"
private val menuEntriesDir = File(buildDir, "generated/menu_entries/src/main")
private val configurationFileReader = rootProject.file(CONFIGURATION_FILE_NAME).reader()
private val configuration: Configuration = Gson().fromJson(configurationFileReader, Configuration::class.java)

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.android.gms.oss-licenses-plugin")
    id("io.sentry.android.gradle") version "3.14.0"
    id("com.mikepenz.aboutlibraries.plugin")
}

task(menuEntriesTask) {
    MenuEntriesGenerator.generate(menuEntriesDir)
}

project.tasks.preBuild.dependsOn(menuEntriesTask)

android {
    namespace = configuration.application_id
    compileSdk = 34

    signingConfigs {
        getByName("debug") {
            keyAlias = "debug"
            keyPassword = "123456"
            storeFile = file("../keystore-debug.jks")
            storePassword = "123456"
        }

        create("release") {
            keyAlias = configuration.android_signing_key_alias
            keyPassword = configuration.android_signing_key_password
            storeFile = file("../keystore-release.jks")
            storePassword = configuration.android_keystore_password
        }
    }

    defaultConfig {
        applicationId = configuration.application_id
        minSdk = 26
        targetSdk = 34
        versionCode = properties.getOrDefault("buildNumber", 1)?.toString()?.toIntOrNull()
        versionName = properties.getOrDefault("versionName", "1")?.toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "app_name", configuration.app_name)

        buildConfigField("String", "CLIENT_ID", "\"" + configuration.client_id + "\"")
        buildConfigField("String", "REDIRECT_URI", "\"cofu-app://callback\"")
        // TODO: buildConfigField("String", "REDIRECT_URI", "\"${configuration.client_id}://callback\"")
        buildConfigField("@androidx.annotation.Nullable String", "DEFAULT_IDP", configuration.default_idp?.let { "\"" + it + "\"" }.toString())
        buildConfigField("Boolean", "HIDE_PRICES", configuration.hide_prices.toString())
        buildConfigField("Boolean", "ONBOARDING_SHOW_CUSTOM_HEADER", configuration.onboarding_show_custom_header.toString())
        buildConfigField("Boolean", "HOME_SHOW_CUSTOM_HEADER", configuration.home_show_custom_header.toString())
        buildConfigField("Boolean", "ANALYTICS_ENABLED", configuration.analytics_enabled.toString())
        buildConfigField("String", "PRIMARY_COLOR", "\"" + configuration.primary_branding_color + "\"")
        buildConfigField("String", "SECONDARY_COLOR", "\"" + configuration.secondary_branding_color + "\"")

        // appAuthRedirectScheme is needed for AppAuth in IDKit and pace_redirect_scheme is needed for deep linking in AppKit
        manifestPlaceholders["appAuthRedirectScheme"] = "cofu-app"
        // TODO: manifestPlaceholders["appAuthRedirectScheme"] = configuration.client_id
        manifestPlaceholders["pace_redirect_scheme"] = "${configuration.client_id}.${UUID.randomUUID()}"
        manifestPlaceholders["google_maps_api_key"] = configuration.google_maps_api_key

        resourceConfigurations += arrayOf("en", "cs", "de", "es", "fr", "it", "nl", "pl", "pt", "ro", "ru")

        // Setup crash reporting
        buildConfigField("Boolean", "SENTRY_ENABLED", configuration.sentry_enabled.toString())
        buildConfigField("@androidx.annotation.Nullable String", "SENTRY_DSN", configuration.sentry_dsn_android?.let { "\"" + it + "\"" }.toString())

        val crashlyticsEnabled = configuration.crashlytics_enabled
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
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            signingConfig = signingConfigs.getByName("debug")
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

    sourceSets {
        getByName("main") {
            java.srcDir(File(menuEntriesDir, "java"))
            res.srcDir(File(menuEntriesDir, "res"))
        }
    }
}

dependencies {
    // PACE Cloud SDK
    implementation("cloud.pace:sdk:21.0.1")

    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0-rc01")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.github.composeuisuite:ohteepee:1.0.3")

    // Google
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-android-compiler:2.48.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    // AboutLibraries
    implementation("com.mikepenz:aboutlibraries-core:10.9.2")
    implementation("com.mikepenz:aboutlibraries-compose:10.9.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    kaptTest("com.google.dagger:hilt-android-compiler:2.48.1")
}

sentry {
    // This is required for the Sentry Gradle plugin to upload Proguard mappings.
    // All other configurations required for the upload are stored as CI variables to not expose them here.
    projectName.set("whitelabel-app") // TODO: change to configuration.sentry_project_name
    includeProguardMapping.set(configuration.sentry_enabled)
}
