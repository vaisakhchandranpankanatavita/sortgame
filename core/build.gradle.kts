plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.badlogicgames.gdx:gdx:${project.property("gdxVersion")}")
    implementation("com.badlogicgames.gdx:gdx-box2d:${project.property("box2DVersion")}")
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

sourceSets {
    main {
        kotlin.srcDirs("src/main/kotlin")
    }
    test {
        kotlin.srcDirs("src/test/kotlin")
    }
}

tasks.test {
    useJUnitPlatform()
}
