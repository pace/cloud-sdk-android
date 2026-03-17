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

        // PACE Android API Kit
        maven {
            val ciJobToken = System.getenv("CI_JOB_TOKEN")
            val localProperties = java.util.Properties().apply {
                settingsDir.resolve("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
            }

            url = uri("https://git.pace.cloud/api/v4/projects/1364/packages/maven")
            credentials(HttpHeaderCredentials::class) {
                name = if (ciJobToken != null) "Job-Token" else "Private-Token"
                value = ciJobToken ?: localProperties.getProperty("gitLabPrivateToken")
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }

        maven("https://jitpack.io")
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/")
        mavenLocal()
    }
}

rootProject.name = "Connected Fueling app"
include(":app")
