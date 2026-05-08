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

rootProject.name = "yingrensheng-android"

include(":app")
include(":benchmark")
include(":sync")

include(":core:analytics")
include(":core:common")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:media")
include(":core:model")
include(":core:navigation")
include(":core:network")
include(":core:testing")
include(":core:ui")
include(":core:upload")

include(":data:agency")
include(":data:creation")
include(":data:member")
include(":data:order")
include(":data:project")
include(":data:user")
include(":data:work")

include(":feature:agency")
include(":feature:auth")
include(":feature:create")
include(":feature:editor")
include(":feature:home")
include(":feature:member")
include(":feature:onboarding")
include(":feature:order")
include(":feature:profile")
include(":feature:works")
