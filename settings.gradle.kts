pluginManagement {
    repositories {
        // Android Gradle Plugin 같은 Gradle 플러그인을 찾는 저장소
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // play-services-auth, Firebase, AndroidX 같은 앱 라이브러리를 찾는 저장소
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repository.map.naver.com/archive/maven")
        maven("https://www.myget.org/F/abtsoftware-bleeding-edge/maven")
        maven("https://www.myget.org/F/abtsoftware/maven")
        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
    }
}

rootProject.name = "GoodMorning"
include(":app")
 