import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("android/keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.sortescape.game"
    compileSdk = project.property("androidCompileSdk").toString().toInt()
    buildToolsVersion = project.property("androidBuildToolsVersion").toString()

    defaultConfig {
        applicationId = "com.sortescape.game"
        minSdk = project.property("androidMinSdk").toString().toInt()
        targetSdk = project.property("androidTargetSdk").toString().toInt()
        versionCode = 2
        versionName = "1.0.1"
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDirs("src/main/kotlin")
            res.srcDirs("res")
            assets.srcDirs("assets")
        }
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/robovm/ios/robovm.xml",
            "META-INF/DEPENDENCIES"
        )
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.isNotEmpty()) {
                storeFile = rootProject.file("android/${keystoreProperties.getProperty("storeFile")}")
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    // Google's official public *test* AdMob IDs (safe to build with, never earn real money,
    // documented at https://developers.google.com/admob/android/test-ads). Both build types
    // default to these so the app is always buildable and ads always work for testing.
    // Before a real store release: create an AdMob account at https://apps.admob.com,
    // register this app (package com.sortescape.game), and replace the three "release"
    // values below with your own App ID / rewarded / interstitial ad unit IDs.
    val testAdMobAppId = "ca-app-pub-3940256099942544~3347511713"
    val testRewardedUnitId = "ca-app-pub-3940256099942544/5224354917"
    val testInterstitialUnitId = "ca-app-pub-3940256099942544/1033173712"

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = testAdMobAppId
            buildConfigField("String", "ADMOB_REWARDED_UNIT_ID", "\"$testRewardedUnitId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"$testInterstitialUnitId\"")
        }
        release {
            isMinifyEnabled = false
            if (keystoreProperties.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
            // TODO before shipping for real: swap these three for your own AdMob IDs.
            manifestPlaceholders["admobAppId"] = testAdMobAppId
            buildConfigField("String", "ADMOB_REWARDED_UNIT_ID", "\"$testRewardedUnitId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"$testInterstitialUnitId\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = false
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))

    val gdxVersion = project.property("gdxVersion")
    val box2DVersion = project.property("box2DVersion")

    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-box2d:$box2DVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:$box2DVersion:natives-armeabi-v7a")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:$box2DVersion:natives-arm64-v8a")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:$box2DVersion:natives-x86")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:$box2DVersion:natives-x86_64")

    implementation("com.google.android.gms:play-services-ads:23.6.0")
}
