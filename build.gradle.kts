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

    listOf(macosX64(), macosArm64()).forEach { target ->
        target.binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.ajalt.clikt:clikt:4.0.0")
                implementation("com.squareup.okio:okio:3.3.0")
                implementation("io.ktor:ktor-client-core:2.3.2")
                implementation("io.ktor:ktor-client-logging:2.3.2")
                implementation("io.ktor:ktor-server-content-negotiation:2.3.2")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
            }
        }

        val macosX64Main by getting
        val macosArm64Main by getting
        val macosMain by creating {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:2.3.2")
            }
        }
        macosMain.dependsOn(commonMain)
        macosX64Main.dependsOn(macosMain)
        macosArm64Main.dependsOn(macosMain)

        // Test source sets
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val macosX64Test by getting
        val macosArm64Test by getting
        val macosTest by creating
        macosTest.dependsOn(commonTest)
        macosX64Test.dependsOn(macosTest)
        macosArm64Test.dependsOn(macosTest)
    }
}

tasks.register<Exec>("assembleReleaseExecutableMacos") {
    dependsOn("linkReleaseExecutableMacosX64", "linkReleaseExecutableMacosArm64")
    commandLine(
        "lipo",
        "-create",
        "-output",
        "kottie",
        "bin/macosX64/releaseExecutable/kottie.kexe",
        "bin/macosArm64/releaseExecutable/kottie.kexe"
    )
    workingDir = buildDir
    group = "Build"
    description = "Builds universal macOS binary"
}
