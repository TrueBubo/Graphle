import org.jetbrains.compose.desktop.application.dsl.TargetFormat


plugins {
    val kotlinVersion = "2.1.21"
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.apollographql.apollo") version "4.2.0"
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
}

group = "com.graphle"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}


dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.apollographql.apollo:apollo-runtime:4.2.0")

    implementation("commons-io:commons-io:2.20.0")
    implementation("org.yaml:snakeyaml:2.0")

    val ktorVersion = "3.3.2"
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")

}

apollo {
    service("service") {
        packageName.set("com.graphle")
    }
}

compose.desktop {
    application {
        mainClass = "com.graphle.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "GraphleUI"
            packageVersion = "1.0.0"
        }
    }
}
