plugins {
    kotlin("jvm") version "2.4.20-RC3"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(project(":common-dto"))
}

kotlin {
    jvmToolchain(26)
}

tasks {

    shadowJar {
        archiveClassifier.set("")
        // Relocate Kotlin stdlib and Gson to prevent plugin classpath conflicts
        relocate("kotlin", "com.sentinel.plugin.libs.kotlin")
        relocate("com.google.gson", "com.sentinel.plugin.libs.gson")
        relocate("kotlinx.serialization", "com.sideplanetary.plugin.libs.serialization")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to project.version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

