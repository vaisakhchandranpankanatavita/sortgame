plugins {
    id("com.android.application") version "8.4.0" apply false
    kotlin("jvm") version "1.9.23" apply false
    kotlin("android") version "1.9.23" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
