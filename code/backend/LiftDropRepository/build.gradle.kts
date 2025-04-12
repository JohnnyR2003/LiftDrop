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
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    implementation("org.jdbi:jdbi3-sqlobject:3.43.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
