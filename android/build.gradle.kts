plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.sortescape.game"
    compileSdk = project.property("androidCompileSdk").toString().toInt()
    buildToolsVersion = project.property("androidBuildToolsVersion").toString()

    defaultConfig {
        applicationId = "com.sortescape.game"
        minSdk = project.property("androidMinSdk").toString().toInt()
        targetSdk = project.property("androidTargetSdk").toString().toInt()
        versionCode = 1
        versionName = "1.0.0"
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

    buildTypes {
        release {
            isMinifyEnabled = false
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
