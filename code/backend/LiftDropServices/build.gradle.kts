plugins {
    kotlin("jvm") version "1.9.25"
}

group = "pt.isel.liftdrop"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":LiftDropDomain"))

    // To use the named annotation
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
