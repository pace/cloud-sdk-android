// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    plugins {
        id(Libs.KTLINT_GRADLE_PLUGIN) version Versions.KTLINT_GRADLE_PLUGIN
    }

    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        classpath(Libs.ANDROID_GRADLE_PLUGIN)
        classpath(Libs.KOTLIN_GRADLE_PLUGIN)
        classpath(Libs.HILT_GRADLE_PLUGIN)
        classpath(Libs.NAVIGATION_SAFE_ARGS)
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
        mavenLocal()
    }

    extra["signing.keyId"] = properties["signingKeyId"]
    extra["signing.password"] = properties["signingPassword"]
    extra["signing.secretKeyRingFile"] = properties["signingSecretKeyRingFile"]
    extra["ossrhUsername"] = properties["ossrhUsername"]
    extra["ossrhPassword"] = properties["ossrhPassword"]
    extra["sonatypeStagingProfileId"] = properties["sonatypeStagingProfileId"]
}

subprojects {
    apply(plugin = Libs.KTLINT_GRADLE_PLUGIN)

    // Adds pre-commit git hook to run ktlintFormat on changed files to .git/hooks/pre-commit. Only copies hook for assembleDebug and assembleRelease and not for testing assemble tasks.
    // Also runs ktlintApplyToIdea to generate Kotlin style files in the project .idea/ folder.
    tasks.getByPath("$path:assembleDebug").dependsOn(":addKtlintFormatGitPreCommitHook", ":ktlintApplyToIdea")
    tasks.getByPath("$path:assembleRelease").dependsOn(":addKtlintFormatGitPreCommitHook", ":ktlintApplyToIdea")
}

nexusStaging {
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
    packageGroup = Config.GROUP_ID
    stagingProfileId = properties["sonatypeStagingProfileId"]?.toString().orEmpty()
    username = properties["ossrhUsername"]?.toString().orEmpty()
    password = properties["ossrhPassword"]?.toString().orEmpty()
}
