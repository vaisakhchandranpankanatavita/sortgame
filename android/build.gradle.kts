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

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystoreProperties.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
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
}
