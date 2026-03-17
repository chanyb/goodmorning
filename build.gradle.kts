// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // 모든 구간에서 사용하기 위해 등록
    id("com.android.application") version "8.9.3" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
}

buildscript {
    val hiltVersion by extra("2.44")
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")
    }
}