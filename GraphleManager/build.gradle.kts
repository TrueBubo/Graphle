plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.netflix.dgs.codegen") version "7.0.3"
}

group = "com.graphle"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-neo4j")
	implementation("org.springframework.boot:spring-boot-starter-graphql")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.generateJava {
	schemaPaths.add("${projectDir}/src/main/resources/graphql-client")
	packageName = "com.graphle.graphlemanager.codegen"
	generateClient = true
}

tasks.withType<Test> {
	useJUnitPlatform()
}
