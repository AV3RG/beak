import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.23"
    id("java-gradle-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "gg.rohan"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.github.mwiede:jsch:0.2.18")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}

kotlin {
    jvmToolchain(8)
}

val relocatePrefix = "gg.rohan.beak.libs"
fun relocateDep(from: String, to: String = "${relocatePrefix}.${from.split(".").last()}"): ShadowJar {
    return tasks.shadowJar.get().relocate(from, to)
}

tasks.register("loadPropertiesFromFile") {
    doLast {
        val file = File(project.rootDir, ".env")
        if (!file.exists()) {
            throw IllegalStateException("local.properties does not exist")
        }
        file.readLines().forEach {
            val (key, value) = it.split("=")
            System.setProperty(key, value)
        }
    }
}

tasks.register("loadPropertiesFromEnv") {
    doLast {
        sequenceOf(
            Pair("GRADLE_PUBLISH_KEY", "gradle.publish.key"),
            Pair("GRADLE_PUBLISH_SECRET", "gradle.publish.secret")
        ).forEach {
            System.setProperty(it.second, System.getenv(it.first)!!)
        }
    }
}

tasks {
    shadowJar {
        archiveBaseName.set("beak")
        archiveClassifier.set("")
        archiveVersion.set("")

        val inclusions = sequenceOf(
            "gg/rohan/beak",
            "com/github/mwiede/jsch",
            "com/squareup/retrofit2/converter/gson",
            "com/squareup/retrofit2/retrofit",
            "com/squareup/okhttp3/okhttp",
            "com/squareup/okio/okio",
            "org/jetbrains/kotlinx/kotlinx-coroutines-core",
            "com/google/code/gson/gson",
        )
        .map { it.split('/' ) }
        .map { sections ->
            var inc = 0
            sections.fold(mutableListOf<String>()) { acc, it ->
                for (a in acc.filter { it.split('/').size == inc }) {
                    acc += "$a/$it"
                }
                acc += it
                inc++
                acc
            }

        }
        .flatten().toMutableList()
        inclusions.add("META-INF")

        include { dep ->
            inclusions.any { dep.path.startsWith(it) }
        }

        relocateDep("retrofit2")
        relocateDep("okhttp3")
        relocateDep("okio")
        relocateDep("kotlinx")
        relocateDep("com.jcraft.jsch")
        relocateDep("com.google")
    }

    assemble {
        dependsOn(shadowJar)
    }

    publishPlugins {
        dependsOn(
            when(System.getenv("PROPS_LOADING")?.lowercase()) {
                "env" -> {"loadPropertiesFromEnv"}
                else -> {"loadPropertiesFromFile"}
            }
        )
    }

}

gradlePlugin {
    website.set("https://github.com/AV3RG/beak")
    vcsUrl.set("https://github.com/AV3RG/beak")
    plugins {
        create("beak") {
            id = "gg.rohan.beak"
            implementationClass = "gg.rohan.beak.BeakPlugin"
            displayName = "Beak"
            description = "Gradle plugin to help with deployment to remote servers on pterodactyl"
            tags.set(listOf(
                "Pterodactyl",
                "Deployment",
                "Remote",
                "Server",
                "MineCraft",
                "Spigot",
                "Paper",
                "BungeeCord",
                "Waterfall",
                "Velocity"
            ))
        }
    }
}