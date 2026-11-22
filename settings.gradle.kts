pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // mtg-sdk-kotlin is only published through JitPack (its original JCenter repo is gone).
        maven("https://jitpack.io") {
            content { includeGroup("com.github.MagicTheGathering") }
        }
    }
}

rootProject.name = "TCG-Manager"
include(":app")
