plugins {
    kotlin("jvm") version "1.9.25"
}

group = "pt.isel.liftdrop"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    api("org.springframework.security:spring-security-core:6.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // JSON format support
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
