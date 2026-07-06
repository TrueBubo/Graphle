plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.netflix.dgs.codegen") version "8.0.4"
	kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.20"
}

group = "com.graphle"
version = "0.0.1-SNAPSHOT"

allprojects {
    group = "com.graphle"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "io.spring.dependency-management")

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.5")
        }
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    dependencies {
        add("implementation", kotlin("stdlib-jdk8"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":model"))
    implementation(project(":tag"))
    implementation(project(":connection"))
    implementation(project(":autocomplete"))
    implementation(project(":file"))
    implementation(project(":application"))
    implementation(project(":dsl"))

    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("commons-io:commons-io:2.20.0")
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation(kotlin("test"))
}

project(":common") {
    dependencies {
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }
}

project(":model") {
    dependencies {
        add("api", project(":common"))
        add("implementation", "org.springframework.boot:spring-boot-starter-data-neo4j")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    }
}

project(":tag") {
    dependencies {
        add("api", project(":common"))
        add("api", project(":model"))
        add("implementation", "org.springframework.boot:spring-boot-starter-data-neo4j")
        add("implementation", "org.springframework.boot:spring-boot-starter-graphql")
    }
}

project(":connection") {
    dependencies {
        add("api", project(":common"))
        add("api", project(":model"))
        add("implementation", "org.springframework.boot:spring-boot-starter-data-neo4j")
        add("implementation", "org.springframework.boot:spring-boot-starter-graphql")
    }
}

project(":autocomplete") {
    dependencies {
        add("api", project(":common"))
        add("implementation", "org.springframework.boot:spring-boot-starter")
        add("implementation", "io.valkey:valkey-java:5.4.0")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }
}

project(":file") {
    dependencies {
        add("api", project(":common"))
        add("api", project(":model"))
        add("implementation", project(":autocomplete"))
        add("implementation", "org.springframework.boot:spring-boot-starter-data-neo4j")
        add("implementation", "org.springframework.boot:spring-boot-starter-graphql")
        add("implementation", "org.springframework.boot:spring-boot-starter-web")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        add("implementation", "commons-io:commons-io:2.20.0")
    }
}

project(":application") {
    dependencies {
        add("api", project(":common"))
        add("api", project(":model"))
        add("implementation", project(":tag"))
        add("implementation", project(":connection"))
        add("implementation", project(":file"))
        add("implementation", "org.springframework.boot:spring-boot-starter-graphql")
    }
}

project(":dsl") {
    dependencies {
        add("api", project(":common"))
        add("api", project(":model"))
        add("implementation", project(":tag"))
        add("implementation", project(":connection"))
        add("implementation", project(":file"))
        add("implementation", project(":application"))
        add("implementation", project(":autocomplete"))
        add("implementation", "org.springframework.boot:spring-boot-starter-data-neo4j")
        add("implementation", "org.springframework.boot:spring-boot-starter-web")
        add("implementation", "org.springframework.boot:spring-boot-starter-websocket")
        add("implementation", "com.fasterxml.jackson.module:jackson-module-kotlin")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        add("implementation", "io.valkey:valkey-java:5.4.0")
        add("testImplementation", kotlin("test"))
    }
}

tasks.generateJava {
	schemaPaths.add("${projectDir}/src/main/resources/graphql-client")
	packageName = "com.graphle.graphlemanager.codegen"
	generateClient = true
}

tasks.withType<Test>().configureEach {
	maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}
tasks.withType<Test>().configureEach {
	reports.html.required = false
	reports.junitXml.required = false
}

tasks.withType<Test> {
	useJUnitPlatform()
}
