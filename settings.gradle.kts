pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven("https://jitpack.io")
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/")
        mavenLocal()
    }
}

rootProject.name = "Connected Fueling app"
include(":app")
