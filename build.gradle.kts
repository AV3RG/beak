import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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

val relocatePrefix = "gg.rohan.beak.libs"
fun relocateDep(from: String, to: String = "${relocatePrefix}.${from.split(".").last()}"): ShadowJar {
    return tasks.shadowJar.get().relocate(from, to)
}

tasks {
    shadowJar {
        archiveBaseName.set("beak")
        archiveClassifier.set("")
        archiveVersion.set("")

        val inclusions = sequenceOf("gg/rohan/beak", "com/github/mwiede/jsch", "com/squareup/retrofit2/retrofit", "com/squareup/okhttp3/okhttp", "com/squareup/okio/okio","org/jetbrains/kotlinx/kotlinx-coroutines-core")
        .map { it.split('/' ) }
        .map {
            var inc = 0
            it.fold(mutableListOf<String>()) { acc, it ->
                for (a in acc.filter { it.split('/').size == inc }) {
                    acc += "$a/$it"
                }
                acc += it
                inc++
                acc
            }

        }
        .flatten().toList()

        include { dep ->
            inclusions.any { dep.path.startsWith(it) }
        }

        relocateDep("retrofit2")
        relocateDep("okhttp3")
        relocateDep("okio")
        relocateDep("kotlinx")
        relocateDep("com.jcraft.jsch")

    }

    assemble {
        dependsOn(shadowJar)
    }

}