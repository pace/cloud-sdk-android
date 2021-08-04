// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
        mavenLocal()
    }

    dependencies {
        classpath(Libs.ANDROID_GRADLE_PLUGIN)
        classpath(Libs.KOTLIN_GRADLE_PLUGIN)
    }
}

plugins {
    id(Libs.DOKKA) version Versions.DOKKA
    id(Libs.NEXUS_STAGING) version Versions.NEXUS_STAGING
    id(Libs.GOOGLE_PROTOBUF_GRADLE_PLUGIN) version Versions.GOOGLE_PROTOBUF_GRADLE_PLUGIN
    java
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        mavenLocal()
    }

    extra["signing.keyId"] = properties["signingKeyId"]
    extra["signing.password"] = properties["signingPassword"]
    extra["signing.secretKeyRingFile"] = properties["signingSecretKeyRingFile"]
    extra["ossrhUsername"] = properties["ossrhUsername"]
    extra["ossrhPassword"] = properties["ossrhPassword"]
    extra["sonatypeStagingProfileId"] = properties["sonatypeStagingProfileId"]
}

nexusStaging {
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
    packageGroup = Config.GROUP_ID
    stagingProfileId = properties["sonatypeStagingProfileId"]?.toString().orEmpty()
    username = properties["ossrhUsername"]?.toString().orEmpty()
    password = properties["ossrhPassword"]?.toString().orEmpty()
}
