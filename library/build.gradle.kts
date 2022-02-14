import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    kotlin("kapt")
    id(Libs.DOKKA)
    id(Libs.GOOGLE_PROTOBUF_GRADLE_PLUGIN)
    `maven-publish`
    signing
}

android {
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

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }

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
            java.srcDir("${protobuf.protobuf.generatedFilesBaseDir}/main/javalite")
            java.srcDir("$projectDir/src/main/proto")
        }
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + listOf("-module-name", "${Config.GROUP_ID}.${Config.ARTIFACT_ID}")
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
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
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Kotlin
    implementation(Libs.KOTLIN_COROUTINES_ANDROID)

    // Android
    implementation(Libs.CORE_KTX)
    implementation(Libs.APPCOMPAT)
    implementation(Libs.CONSTRAINT_LAYOUT)
    implementation(Libs.PREFERENCE_KTX)
    implementation(Libs.FRAGMENT_KTX)
    implementation(Libs.LIFECYCLE_LIVE_DATA_KTX)
    implementation(Libs.LIFECYCLE_PROCESS_KTX)
    implementation(Libs.BIOMETRIC)
    implementation(Libs.BROWSER)
    implementation(Libs.ROOM_RUNTIME)
    kapt(Libs.ROOM_COMPILER)

    // Google
    implementation(Libs.GOOGLE_PLAY_SERVICES_LOCATION)
    api(Libs.GOOGLE_PLAY_SERVICES_MAPS)
    implementation(Libs.GOOGLE_MAPS_UTILS)
    implementation(Libs.GOOGLE_PROTOBUF_JAVALITE)

    // Dependency injection
    api(Libs.KOIN_ANDROID)
    api(Libs.KOIN_CORE)

    // Networking
    api(Libs.RETROFIT)
    implementation(Libs.RETROFIT_CONVERTER_MOSHI)
    implementation(Libs.RETROFIT_CONVERTER_GSON)
    implementation(Libs.RETROFIT_ADAPTER_RXJAVA)
    implementation(Libs.OKHTTP_LOGGING_INTERCEPTOR)
    implementation(Libs.MOSHI_KOTLIN)
    implementation(Libs.MOSHI_ADAPTERS)
    api(Libs.MOSHI_JSONAPI)
    implementation(Libs.MOSHI_JSONAPI_RETROFIT_CONVERTER)
    implementation(Libs.GSON)
    api(Libs.RXJAVA)
    api(Libs.RXANDROID)

    // Others
    api(Libs.APPAUTH)
    implementation(Libs.ONE_TIME_PASSWORD)
    implementation(Libs.COMMONS_CODEC)
    implementation(Libs.TIMBER)

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
    androidTestImplementation(Libs.TEST_RUNNER)
    androidTestImplementation(Libs.TEST_JUNIT)
    api(Libs.ROOM_TESTING)
}

tasks.withType<ProcessResources> {
    exclude("**/*.proto")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.17.3"
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
val sourcesJar by tasks.creating(Jar::class) {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}

val javadoc by tasks.creating(Javadoc::class) {
    source(android.sourceSets["main"].java.srcDirs)
    classpath += project.files(android.bootClasspath.joinToString(File.separator))
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(dokkaHtml)
    group = "jar"
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}

configurations.create("javadocDeps")

group = Config.GROUP_ID
version = properties.getOrDefault("versionName", Versions.DEFAULT_VERSION_NAME_LIBRARY)!!.toString()

publishing {
    publications {
        create<MavenPublication>("debug") {
            groupId = Config.GROUP_ID
            artifactId = Config.ARTIFACT_ID
            version = properties.getOrDefault("versionName", Versions.DEFAULT_VERSION_NAME_LIBRARY)!!.toString()

            artifact("$buildDir/outputs/aar/${project.name}-debug.aar") {
                builtBy(tasks.getByName("assemble"))
            }
            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name.set("PACE Cloud SDK Android")
                description.set("PACE Cloud SDK is a client SDK that allows your app to easily connect to PACE\'s Connected Fueling. The SDK consists of the IDKit to manage the OpenID (OID) authorization and general session flow with its token handling. It also consists of the AppKit, with which you can fetch and display location based apps, apps by URL or ID. Furthermore it contains the POIKit, which allows you to fetch Point of Interests (e.g. gas stations), request routes and search for locations.")
                url.set("https://github.com/pace/cloud-sdk-android")
                licenses {
                    license {
                        name.set("The MIT License (MIT)")
                        url.set("https://github.com/pace/cloud-sdk-android/blob/master/LICENSE.md")
                    }
                }
                developers {
                    developer {
                        name.set("PACE Telematics GmbH")
                        email.set("android-dev@pace.car")
                        organization.set("PACE Telematics GmbH")
                        organizationUrl.set("https://www.pace.car")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/pace/cloud-sdk-android.git")
                    developerConnection.set("scm:git:ssh://github.com/pace/cloud-sdk-android.git")
                    url.set("https://github.com/pace/cloud-sdk-android/tree/master")
                }
                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")

                    project.configurations.implementation.get().allDependencies.forEach {
                        if (it.name != "unspecified") {
                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", it.group)
                            dependencyNode.appendNode("artifactId", it.name)
                            dependencyNode.appendNode("version", it.version)
                        }
                    }
                }
            }
        }
        create<MavenPublication>("release") {
            groupId = Config.GROUP_ID
            artifactId = Config.ARTIFACT_ID
            version = properties.getOrDefault("versionName", Versions.DEFAULT_VERSION_NAME_LIBRARY)!!.toString()

            artifact("$buildDir/outputs/aar/${project.name}-release.aar") {
                builtBy(tasks.getByName("assemble"))
            }
            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name.set("PACE Cloud SDK Android")
                description.set("PACE Cloud SDK is a client SDK that allows your app to easily connect to PACE\'s Connected Fueling. The SDK consists of the IDKit to manage the OpenID (OID) authorization and general session flow with its token handling. It also consists of the AppKit, with which you can fetch and display location based apps, apps by URL or ID. Furthermore it contains the POIKit, which allows you to fetch Point of Interests (e.g. gas stations), request routes and search for locations.")
                url.set("https://github.com/pace/cloud-sdk-android")
                licenses {
                    license {
                        name.set("The MIT License (MIT)")
                        url.set("https://github.com/pace/cloud-sdk-android/blob/master/LICENSE.md")
                    }
                }
                developers {
                    developer {
                        name.set("PACE Telematics GmbH")
                        email.set("android-dev@pace.car")
                        organization.set("PACE Telematics GmbH")
                        organizationUrl.set("https://www.pace.car")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/pace/cloud-sdk-android.git")
                    developerConnection.set("scm:git:ssh://github.com/pace/cloud-sdk-android.git")
                    url.set("https://github.com/pace/cloud-sdk-android/tree/master")
                }
                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")

                    project.configurations.implementation.get().allDependencies.forEach {
                        if (it.name != "unspecified") {
                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", it.group)
                            dependencyNode.appendNode("artifactId", it.name)
                            dependencyNode.appendNode("version", it.version)
                        }
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "mavenCentral"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = extra["ossrhUsername"]?.toString().orEmpty()
                password = extra["ossrhPassword"]?.toString().orEmpty()
            }
        }
    }
}

signing {
    // Signing is only mandatory for release publications.
    // Publish to mavenLocal can be executed with the publishDebugPublicationToMavenLocal Gradle task (unsigned).
    sign(publishing.publications["release"])
}
