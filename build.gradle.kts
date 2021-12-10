plugins {
    // "org.jetbrains.kotlin.jvm"
    kotlin("jvm") version "1.5.32" apply false

    // "org.jetbrains.kotlin.kapt"
    kotlin("kapt") version "1.5.32" apply false

    // "kotlinx-serialization"
    kotlin("plugin.serialization") version "1.5.31"

    id("org.jetbrains.intellij") version "1.3.0" apply false
}

allprojects { repositories { mavenLocal() } }

subprojects {
    if (name == "contracts") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "org.jetbrains.kotlin.kapt")
        apply(plugin = "kotlinx-serialization")
    }

    if (name == "merge-request-integration") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "org.jetbrains.kotlin.kapt")
        apply(plugin = "kotlinx-serialization")
    }

    if (name == "merge-request-integration-core") {
        apply(plugin = "org.jetbrains.intellij")
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }

    if (name == "merge-request-integration-ce") {
        apply(plugin = "org.jetbrains.intellij")
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }

    if (name == "merge-request-integration-ee") {
        apply(plugin = "org.jetbrains.intellij")
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }
}