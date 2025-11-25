plugins {
	java
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "world.erv"
version = "0.0.1-SNAPSHOT"
description = "Examine current topical trends across the internet"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Spring Data Reactive (R2DBC)
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // for flyway
    runtimeOnly("org.postgresql:postgresql")

    // R2DBC Drivers
    implementation("org.postgresql:r2dbc-postgresql")

    implementation("com.google.genai:google-genai:1.28.0")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()

    minHeapSize = "2g"
    maxHeapSize = "4g"
}
