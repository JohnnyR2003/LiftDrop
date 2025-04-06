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

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

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
