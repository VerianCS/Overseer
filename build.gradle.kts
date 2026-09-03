plugins {
    kotlin("jvm") version "2.4.20-RC3"
}

group = "com.enderstorage"
version = "0.0.1-SNAPSHOT"
description = "Overseer"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(26))
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

