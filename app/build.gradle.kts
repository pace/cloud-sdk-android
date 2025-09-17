import car.pace.cofu.configuration.CONFIGURATION_FILE_NAME
import car.pace.cofu.configuration.Configuration
import car.pace.cofu.menu.MenuEntriesGenerator
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.google.gson.Gson
import java.util.UUID

private val menuEntriesTask = "generateMenuEntries"
private val menuEntriesDir = layout.buildDirectory.file("generated/menu_entries/src/main").get().asFile
private val configurationFileReader = rootProject.file(CONFIGURATION_FILE_NAME).reader()
private val configuration: Configuration = Gson().fromJson(configurationFileReader, Configuration::class.java)

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("io.sentry.android.gradle") version "5.11.0"
    id("com.mikepenz.aboutlibraries.plugin")
}

task(menuEntriesTask) {
    MenuEntriesGenerator.generate(menuEntriesDir)
}

project.tasks.preBuild.dependsOn(menuEntriesTask)

android {
    namespace = "car.pace.cofu"
    compileSdk = 35

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
        applicationId = configuration.application_id_android
        minSdk = 26
        targetSdk = 35
        versionCode = properties.getOrDefault("buildNumber", 1)?.toString()?.toIntOrNull()
        versionName = properties.getOrDefault("versionName", "1")?.toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "app_name", configuration.app_name)
        resValue("color", "notification_color", configuration.primary_branding_color)
        resValue("string", "google_maps_api_key", configuration.google_maps_api_key)

        buildConfigField("String", "CLIENT_ID", "\"" + configuration.client_id + "\"")
        buildConfigField("String", "REDIRECT_URI", "\"${configuration.client_id}://callback\"")
        buildConfigField("@androidx.annotation.Nullable String", "DEFAULT_IDP", configuration.default_idp?.let { "\"" + it + "\"" }.toString())
        buildConfigField("Boolean", "HIDE_PRICES", configuration.hide_prices.toString())
        buildConfigField("Boolean", "ONBOARDING_SHOW_CUSTOM_HEADER", configuration.onboarding_show_custom_header.toString())
        buildConfigField("Boolean", "LIST_SHOW_CUSTOM_HEADER", configuration.list_show_custom_header.toString())
        buildConfigField("Boolean", "DETAIL_SCREEN_SHOW_ICON", configuration.detail_screen_show_icon.toString())
        buildConfigField("Boolean", "MAP_ENABLED", configuration.map_enabled.toString())
        buildConfigField("Boolean", "AUTOMATIC_PRODUCTION_UPDATES_ENABLED", configuration.automatic_production_updates_enabled.toString())
        buildConfigField("Boolean", "NATIVE_FUELCARD_MANAGEMENT_ENABLED", configuration.native_fuelcard_management_enabled.toString())
        buildConfigField("Boolean", "VEHICLE_INTEGRATION_ENABLED", configuration.vehicle_integration_enabled.toString())
        buildConfigField("String", "PRIMARY_COLOR", "\"" + configuration.primary_branding_color + "\"")
        buildConfigField("String", "SECONDARY_COLOR", "\"" + configuration.secondary_branding_color + "\"")

        androidResources.localeFilters += arrayOf("en", "cs", "de", "es", "fr", "it", "nl", "pl", "pt", "ro", "ru")

        // Setup crash and analytics reporting
        buildConfigField("Boolean", "SENTRY_ENABLED", configuration.sentry_enabled.toString())
        buildConfigField("@androidx.annotation.Nullable String", "SENTRY_DSN", configuration.sentry_dsn_android?.let { "\"" + it + "\"" }.toString())

        val crashlyticsEnabled = configuration.crashlytics_enabled
        val analyticsEnabled = configuration.analytics_enabled
        buildConfigField("Boolean", "CRASHLYTICS_ENABLED", crashlyticsEnabled.toString())
        buildConfigField("Boolean", "ANALYTICS_ENABLED", analyticsEnabled.toString())

        if (crashlyticsEnabled || analyticsEnabled) {
            apply(plugin = "com.google.gms.google-services")

            if (crashlyticsEnabled) {
                apply(plugin = "com.google.firebase.crashlytics")
            }
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

    flavorDimensions += "environment"

    productFlavors {
        create("development") {
            applicationIdSuffix = ".development"
            dimension = "environment"
            buildConfigField("int", "ENVIRONMENT", "cloud.pace.sdk.utils.Environment.DEVELOPMENT.ordinal()")

            manifestPlaceholders["environment"] = "dev"

            // appAuthRedirectScheme is needed for AppAuth in IDKit and pace_redirect_scheme is needed for deep linking in AppKit
            manifestPlaceholders["appAuthRedirectScheme"] = configuration.client_id
            manifestPlaceholders["pace_redirect_scheme"] = "${configuration.client_id}-dev.${UUID.randomUUID()}"
        }

        create("production") {
            dimension = "environment"
            buildConfigField("int", "ENVIRONMENT", "cloud.pace.sdk.utils.Environment.PRODUCTION.ordinal()")

            manifestPlaceholders["environment"] = "prod"
            // appAuthRedirectScheme is needed for AppAuth in IDKit and pace_redirect_scheme is needed for deep linking in AppKit
            manifestPlaceholders["appAuthRedirectScheme"] = configuration.client_id
            manifestPlaceholders["pace_redirect_scheme"] = "${configuration.client_id}.${UUID.randomUUID()}"
        }

        create("sandbox") {
            applicationIdSuffix = ".sandbox"
            dimension = "environment"
            buildConfigField("int", "ENVIRONMENT", "cloud.pace.sdk.utils.Environment.SANDBOX.ordinal()")

            manifestPlaceholders["environment"] = "sandbox"
            // appAuthRedirectScheme is needed for AppAuth in IDKit and pace_redirect_scheme is needed for deep linking in AppKit
            manifestPlaceholders["appAuthRedirectScheme"] = "${configuration.client_id}-sandbox"
            manifestPlaceholders["pace_redirect_scheme"] = "${configuration.client_id}-sandbox.${UUID.randomUUID()}"
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
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
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
    implementation("cloud.pace:sdk:25.0.0")

    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("com.google.maps.android:maps-compose:4.3.2")
    implementation("com.google.maps.android:maps-compose-utils:4.3.2")
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.github.composeuisuite:ohteepee:1.0.3")
    implementation("androidx.paging:paging-compose:3.3.6")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-android-compiler:2.48.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-inappmessaging-display")

    // Places search
    implementation("com.google.android.libraries.places:places:3.3.0")

    // AboutLibraries
    implementation("com.mikepenz:aboutlibraries-compose:10.10.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    kaptTest("com.google.dagger:hilt-android-compiler:2.48.1")
    testImplementation("io.mockk:mockk:1.13.9")
}

sentry {
    // This is required for the Sentry Gradle plugin to upload Proguard mappings.
    // All other configurations required for the upload are stored as CI variables to not expose them here.
    projectName.set(configuration.sentry_project_name_android)
    includeProguardMapping.set(configuration.sentry_enabled)
}
