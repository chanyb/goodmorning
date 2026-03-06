import java.util.Properties
plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    kotlin("android")
    kotlin("kapt") // kotlin annotation processing tool
}

android {
    namespace = "kr.co.kworks.goodmorning"
    compileSdk = 35

    defaultConfig {
        applicationId = "kr.co.kworks.goodmorning"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(getProperty("STORE_FILE_PATH"))
            storePassword = getProperty("STORE_PASSWORD")
            keyAlias = getProperty("STORE_KEY_ALIAS")
            keyPassword = getProperty("STORE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release");

            resValue("string", "domain", getProperty("OPERATION_DOMAIN"))
            buildConfigField("Boolean", "IS_PRODUCTION", "true")
            isDebuggable = false

            ndk {
                abiFilters += listOf("arm64-v8a")
            }
        }


        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "domain", getProperty("OPERATION_DOMAIN"))
            buildConfigField("Boolean", "IS_PRODUCTION", "false")
        }

        create("devlee") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "domain", getProperty("DEV_LEE_IP"))
            buildConfigField("Boolean", "IS_PRODUCTION", "false")
        }

        create("devnoh") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "domain", getProperty("DEV_NOH_IP"))
            buildConfigField("Boolean", "IS_PRODUCTION", "false")
        }

    }


    buildFeatures {
        buildConfig = true
        // 뷰 바인딩 활성화
        dataBinding = true
//        viewBinding = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        getByName("main").res.srcDir("src/main/res/layouts/")
        getByName("main").res.srcDir("src/main/res/layouts/common")
        getByName("main").res.srcDir("src/main/res/layouts/activity")
        getByName("main").res.srcDir("src/main/res/layouts/view")
        getByName("main").res.srcDir("src/main/res/layouts/dialog")
        getByName("main").res.srcDir("src/main/res/layouts/fragment")
        getByName("main").res.srcDir("src/main/res/layouts/item")
    }
//    packaging {
//        jniLibs {
//            pickFirsts += setOf(
//                // 모든 ABI에 대해 libc++_shared.so가 중복될 때 하나만 선택
//                "lib/**/libc++_shared.so"
//            )
//        }
//    }
}

dependencies {
    // (implementation) 런 타임에서 사용
    // (kapt) 컴파일 타임에 사용

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // vlc
    implementation ("org.videolan.android:libvlc-all:3.5.1")

    // Preferences
    implementation("com.github.rtoshiro.securesharedpreferences:securesharedpreferences:1.2.0")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Hilt
    implementation ("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")

    // SceneView
    implementation("io.github.sceneview:sceneview:2.3.0")

    // osmdroid
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.20")

    // media3 - exoplayer
    val media3Version = "1.8.0"
    implementation("androidx.media3:media3-exoplayer-rtsp:${media3Version}")
    implementation("androidx.media3:media3-exoplayer:${media3Version}")
    implementation("androidx.media3:media3-ui:${media3Version}")
    // RTSP 직원을 위한 확장
    implementation("androidx.media3:media3-exoplayer-rtsp:${media3Version}")
    // RTMP 지원을 위한 확장
    implementation("androidx.media3:media3-datasource-rtmp:${media3Version}")

    val cameraXVersion = "1.5.1"
    implementation("androidx.camera:camera-core:${cameraXVersion}")
    implementation("androidx.camera:camera-camera2:${cameraXVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraXVersion}")
    implementation("androidx.camera:camera-view:${cameraXVersion}")
    // 🎥 VideoCapture & VideoOutput
    implementation("androidx.camera:camera-video:${cameraXVersion}")

    // pedroSG94 RTMP Broadcast
    implementation("com.github.pedroSG94.RootEncoder:library:2.6.2")  // 최신 버전
    implementation("com.github.pedroSG94.RootEncoder:extra-sources:2.6.2") // 카메라X 등 추가 소스
    // pedroSG94 RTSP server
    implementation("com.github.pedroSG94:RTSP-Server:1.3.6")

    // ffmpeg
//    implementation(files("libs/ffmpeg-kit-full-gpl-6.0-2.LTS.aar"))
//    implementation("com.arthenica:smart-exception-java:0.2.1")
    implementation("ai.instavision:ffmpegkit:2025.08.21")
//    implementation("com.antonkarpenko:ffmpeg-kit-full-gpl:2.1.0") // 16KB 미지원 or vlc와 충돌

    // fusedLocationProvider
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Naver map
    implementation("com.naver.maps:map-sdk:3.22.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // leakcanary
//    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

    // lifecycler-service
    implementation ("androidx.lifecycle:lifecycle-service:2.9.4")

    val sciChart_version = "4.6.0.4885"
    implementation ("com.scichart.library:core:${sciChart_version}@aar")
    implementation ("com.scichart.library:data:${sciChart_version}@aar")
    implementation ("com.scichart.library:drawing:${sciChart_version}@aar")
    implementation ("com.scichart.library:charting:${sciChart_version}@aar")
    implementation ("com.scichart.library:data:${sciChart_version}@aar")
    implementation ("com.scichart.library:extensions:${sciChart_version}@aar")
    implementation ("com.scichart.library:data:${sciChart_version}@aar")
}

fun loadLocalProperties(file: File): Properties {
    val properties = Properties()
    if (file.exists()) {
        file.inputStream().use { properties.load(it) }
    }
    return properties
}


fun getProperty(key: String): String {
    val localProperties = loadLocalProperties(rootProject.file("local.properties"))
    return localProperties.getProperty(key)
}