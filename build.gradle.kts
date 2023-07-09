plugins {
    kotlin("multiplatform") version "1.9.0"
    kotlin("plugin.serialization") version "1.8.21"
}

group = "com.iamwent"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation("com.github.ajalt.clikt:clikt:4.0.0")
                implementation("com.squareup.okio:okio:3.3.0")
                implementation("io.ktor:ktor-client-core:2.3.2")
                implementation("io.ktor:ktor-client-darwin:2.3.2")
                implementation("io.ktor:ktor-client-logging:2.3.2")
                implementation("io.ktor:ktor-server-content-negotiation:2.3.2")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
                implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
            }
        }
        val nativeTest by getting
    }
}
