plugins {
    kotlin("jvm") version "1.9.23"
    id("java-gradle-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "gg.rohan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.github.mwiede:jsch:0.2.18")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
}

kotlin {
    jvmToolchain(8)
}

configurations {
    compileClasspath {
        dependencies.remove(project.dependencies.gradleApi())
        dependencies.remove(project.dependencies.localGroovy())
    }
    runtimeClasspath {
        dependencies.remove(project.dependencies.gradleApi())
        dependencies.remove(project.dependencies.localGroovy())
    }
    implementation {
        dependencies.remove(project.dependencies.gradleApi())
        dependencies.remove(project.dependencies.localGroovy())
    }
}

tasks {
    shadowJar {
        archiveBaseName.set("beak")
        archiveClassifier.set("")
        archiveVersion.set("")

        include(
            "gg/rohan/beak/**/*",
            "com/squareup/retrofit2/**/*",
        )

    }

    assemble {
        dependsOn(shadowJar)
    }

}