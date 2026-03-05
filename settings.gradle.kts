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
        maven("https://jitpack.io")
        maven("https://repository.map.naver.com/archive/maven")
        maven("https://www.myget.org/F/abtsoftware-bleeding-edge/maven")
        maven("https://www.myget.org/F/abtsoftware/maven")
    }
}

rootProject.name = "GoodMorning"
include(":app")
 