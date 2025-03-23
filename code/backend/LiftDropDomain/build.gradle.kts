plugins {
    kotlin("jvm") version "1.9.25"
}

group = "pt.isel.liftdrop"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
