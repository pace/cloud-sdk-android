import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    plugins {
        id(Libs.KTLINT_GRADLE_PLUGIN) version Versions.KTLINT_GRADLE_PLUGIN
        id(Libs.JETBRAINS_IDEA_GRADLE_PLUGIN) version Versions.JETBRAINS_IDEA_GRADLE_PLUGIN
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
    id(Libs.NEXUS_PUBLISH) version Versions.NEXUS_PUBLISH
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
}

idea.project.settings {
    taskTriggers {
        // This tasks will be triggered after each Gradle sync:
        // Adds pre-commit git hook to run ktlintFormat on changed files to .git/hooks/pre-commit
        // Also runs ktlintApplyToIdea to generate Kotlin style files in the project .idea/ folder
        afterSync(tasks.getByName("addKtlintFormatGitPreCommitHook"), tasks.getByName("ktlintApplyToIdea"))
    }
}

group = Config.GROUP_ID
version = properties.getOrDefault("versionName", Versions.DEFAULT_VERSION_NAME_LIBRARY)!!.toString()

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            stagingProfileId.set(properties["sonatypeStagingProfileId"]?.toString())
            username.set(properties["ossrhUsername"]?.toString())
            password.set(properties["ossrhPassword"]?.toString())
            repositoryDescription.set("${Config.GROUP_ID}:${Config.ARTIFACT_ID}:${properties["versionName"] ?: Versions.DEFAULT_VERSION_NAME_LIBRARY}")
        }
    }
}
