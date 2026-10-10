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
        mavenCentral()
    }
}

rootProject.name = "toyota-launcher"

include(":app:launcher")
include(":app:cluster")
include(":core:car")
include(":core:design")
include(":feature:hvac")
include(":feature:drivemode")
include(":feature:media")
include(":feature:weather")
include(":feature:apprail")
include(":feature:topstatus")
include(":feature:uxrestrictions")
include(":provider:weather-mock")
