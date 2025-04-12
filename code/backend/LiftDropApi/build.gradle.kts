plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "pt.isel.liftdrop"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":LiftDropDomain"))
    implementation(project(":LiftDropServices"))
    implementation(project(":LiftDropRepositoryJdbi"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // JDBI dependencies
    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.jdbi:jdbi3-kotlin:3.37.1")
    implementation("org.jdbi:jdbi3-postgres:3.37.1")
    implementation("org.postgresql:postgresql:42.7.2")

    // Spring MVC and Servlet API
    implementation("org.springframework.boot:spring-boot-starter-web:3.1.0") // Correct version
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0") // Use the same version as your servlet API

    // SLF4J for logging
    implementation("org.slf4j:slf4j-api:2.0.16")

    // Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // Bean Validation API
    implementation("jakarta.validation:jakarta.validation-api:3.0.0")

    // Hibernate Validator implementation
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")

    // For Hibernate Validator to work with Jakarta Validation
    implementation("org.glassfish:jakarta.el:4.0.2")

    // Testing dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.1.0")
    testImplementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.1.0")
    // testImplementation(kotlin("test"))
    api(project(":LiftDropServices"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
