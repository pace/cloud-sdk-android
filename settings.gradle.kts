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
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        mavenLocal()
    }
}

rootProject.name = "Connected Fueling app"
include(":app")
