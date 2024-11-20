pluginManagement {
    repositories {
        google() // Required for Android and Firebase plugins
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Prefer settings but allow project-level repos if needed
    repositories {
        google() // Required for Android and Firebase dependencies
        mavenCentral()
    }
}

rootProject.name = "Register_App"
include(":app")
