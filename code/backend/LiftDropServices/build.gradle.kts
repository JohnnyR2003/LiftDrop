plugins {
    kotlin("jvm") version "1.9.25"
}

group = "pt.isel.services"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":LiftDropDomain"))
    api(project(":LiftDropRepository"))

    // To use the named annotation
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    // Spring Encoder
    implementation("org.springframework.boot:spring-boot-starter-security:3.4.4")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.0")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(kotlin("test"))
    testImplementation("org.jdbi:jdbi3-core:3.37.1")
    testImplementation("org.postgresql:postgresql:42.7.2")
    testImplementation(project(":LiftDropRepositoryJdbi"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
