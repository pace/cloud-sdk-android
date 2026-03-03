import com.google.protobuf.gradle.id
import org.jreleaser.model.Active
import java.time.LocalDate

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    kotlin("kapt")
    id(Libs.DOKKA)
    id(Libs.GOOGLE_PROTOBUF_GRADLE_PLUGIN)
    `maven-publish`
    id(Libs.JRELEASER_GRADLE_PLUGIN)
}

android {
    namespace = "cloud.pace.sdk"
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        val versionCode = properties.getOrDefault("buildNumber", Versions.DEFAULT_VERSION_CODE_LIBRARY)?.toString()?.toIntOrNull() ?: Versions.DEFAULT_VERSION_CODE_LIBRARY
        val versionName = properties.getOrDefault("versionName", Versions.DEFAULT_VERSION_NAME_LIBRARY) ?: Versions.DEFAULT_VERSION_NAME_LIBRARY
        version = versionName
        buildConfigField("int", "VERSION_CODE", versionCode.toString())
        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")

        testInstrumentationRunner = Libs.TEST_INSTRUMENTATION_RUNNER

        manifestPlaceholders["pace_redirect_scheme"] = "\${pace_redirect_scheme}"
        manifestPlaceholders["appAuthRedirectScheme"] = "\${appAuthRedirectScheme}"

        consumerProguardFile("proguard-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDir("${protobuf.generatedFilesBaseDir}/main/javalite")
            java.srcDir("$projectDir/src/main/proto")
        }
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf("-module-name", "${Config.GROUP_ID}.${Config.ARTIFACT_ID}")
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.JETPACK_COMPOSE_COMPILER
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    packagingOptions {
        resources.merges.add("META-INF/LICENSE.md")
        resources.merges.add("META-INF/LICENSE-notice.md")
    }

    tasks.dokkaHtml.configure {
        outputDirectory.set(buildDir.resolve("dokka"))

        moduleName.set("PACECloudSDK")
        dokkaSourceSets {
            configureEach {
                includeNonPublic.set(false)
            }
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Kotlin
    api(Libs.KOTLIN_COROUTINES_ANDROID)
    api(Libs.KOTLIN_COROUTINES_PLAY_SERVICES)

    // Android
    api(Libs.CORE_KTX)
    api(Libs.APPCOMPAT)
    api(Libs.CONSTRAINT_LAYOUT)
    api(Libs.PREFERENCE_KTX)
    api(Libs.FRAGMENT_KTX)
    api(Libs.LIFECYCLE_LIVE_DATA_KTX)
    api(Libs.LIFECYCLE_PROCESS_KTX)
    api(Libs.BIOMETRIC)
    api(Libs.BROWSER)
    api(Libs.ACTIVITY)

    // Jetpack Compose
    // The api declaration and the animation dependency is needed for clients without Jetpack Compose
    api(Libs.JETPACK_COMPOSE_ANIMATION)
    api(Libs.JETPACK_COMPOSE_FOUNDATION)
    api(Libs.JETPACK_COMPOSE_MATERIAL)
    api(Libs.JETPACK_COMPOSE_UI_TOOLING_PREVIEW)
    debugApi(Libs.JETPACK_COMPOSE_UI_TOOLING)
    api(Libs.LIFECYCLE_RUNTIME_COMPOSE_KTX)
    api(Libs.COIL_COMPOSE)

    // Google
    api(Libs.GOOGLE_PLAY_SERVICES_LOCATION)
    api(Libs.GOOGLE_PLAY_SERVICES_MAPS)
    api(Libs.GOOGLE_MAPS_UTILS)
    api(Libs.GOOGLE_PROTOBUF_JAVALITE)
    api(Libs.GOOGLE_PAY)

    // Dependency injection
    api(Libs.KOIN_ANDROID)
    api(Libs.KOIN_ANDROID_COMPOSE)
    api(Libs.KOIN_CORE)

    // Networking
    api(Libs.RETROFIT)
    api(Libs.RETROFIT_CONVERTER_MOSHI)
    api(Libs.RETROFIT_CONVERTER_GSON)
    api(Libs.RETROFIT_ADAPTER_RXJAVA)
    api(Libs.OKHTTP_LOGGING_INTERCEPTOR)
    api(Libs.MOSHI_KOTLIN)
    api(Libs.MOSHI_ADAPTERS)
    // The moshi-jsonapi dependency is now added as JAR so that JCenter is not used as dependency repository anymore
    api(files(Libs.MOSHI_JSONAPI_JAR))
    api(files(Libs.MOSHI_JSONAPI_RETROFIT_CONVERTER_JAR))
    api(Libs.GSON)
    api(Libs.RXJAVA)
    api(Libs.RXANDROID)

    // Others
    api(Libs.APPAUTH)
    api(Libs.ONE_TIME_PASSWORD)
    api(Libs.COMMONS_CODEC)
    api(Libs.TIMBER)

    // Testing
    testImplementation(Libs.ARCH_TESTING)
    testImplementation(Libs.COROUTINES_TEST)
    testImplementation(Libs.KOIN_TEST)
    testImplementation(Libs.MOCKITO_CORE)
    testImplementation(Libs.MOCKITO_INLINE)
    testImplementation(Libs.MOCKITO_KOTLIN)
    testImplementation(Libs.MOCKK)
    testImplementation(Libs.ROBOLECTRIC)

    androidTestImplementation(Libs.TEST_CORE)
    androidTestImplementation(Libs.TEST_RULES)
    androidTestImplementation(Libs.TEST_RUNNER)
    androidTestImplementation(Libs.JUNIT)
    androidTestImplementation(Libs.KOIN_TEST) {
        exclude("org.mockito")
    }
    androidTestImplementation(Libs.MOCKK_ANDROID)
}

tasks.withType<ProcessResources> {
    exclude("**/*.proto")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.2"
    }
    // this is a task which wil generate classes for our proto files
    generateProtoTasks {
        all().forEach {
            it.builtins {
                id("java") {
                    option("lite")
                }
            }
        }
    }
}

/* Publishing to Maven Central */
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = Config.GROUP_ID
            artifactId = Config.ARTIFACT_ID
            version = properties.getOrDefault("versionName", Versions.DEFAULT_VERSION_NAME_LIBRARY)!!.toString()

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set(Config.SDK_NAME)
                description.set(Config.SDK_DESCRIPTION)
                url.set("https://github.com/pace/cloud-sdk-android")
                licenses {
                    license {
                        name.set("The MIT License (MIT)")
                        url.set("https://github.com/pace/cloud-sdk-android/blob/master/LICENSE.md")
                    }
                }
                developers {
                    developer {
                        name.set(Config.SDK_VEND0R)
                        email.set("android-dev@pace.car")
                        organization.set(Config.SDK_VEND0R)
                        organizationUrl.set("https://www.pace.car")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/pace/cloud-sdk-android.git")
                    developerConnection.set("scm:git:ssh://github.com/pace/cloud-sdk-android.git")
                    url.set("https://github.com/pace/cloud-sdk-android/tree/master")
                }
            }
        }
    }

    repositories {
        maven {
            setUrl(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    gitRootSearch = true

    project {
        author(Config.SDK_VEND0R)
        vendor = Config.SDK_VEND0R
        copyright = "Copyright (c) ${LocalDate.now().year} ${Config.SDK_VEND0R}"
        name = Config.ARTIFACT_ID
        group = Config.GROUP_ID
        description = Config.SDK_DESCRIPTION
        license = "MIT"
        links {
            homepage = "https://github.com/pace/cloud-sdk-android"
            bugTracker = "https://github.com/pace/cloud-sdk-android/issues"
            vcsBrowser = "https://github.com/pace/cloud-sdk-android"
            documentation = "https://docs.pace.cloud/integrating/mobile-app"
            contact = "https://www.pace.car"
        }
    }

    signing {
        active = Active.ALWAYS
        armored = true
        verify = true
    }

    deploy {
        maven {
            mavenCentral {
                register("release-deploy") {
                    active = Active.RELEASE
                    url = "https://central.sonatype.com/api/v1/publisher"
                    applyMavenCentralRules = true
                    verifyPom = false
                    stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                }
            }

            nexus2 {
                register("snapshot-deploy") {
                    active = Active.SNAPSHOT
                    url = "https://central.sonatype.com/repository/maven-snapshots"
                    snapshotUrl = "https://central.sonatype.com/repository/maven-snapshots"
                    applyMavenCentralRules = true
                    verifyPom = false
                    snapshotSupported = true
                    closeRepository = true
                    releaseRepository = true
                    stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                }
            }
        }
    }
}
