plugins {
    kotlin("jvm") version "1.9.25"
}

group = "liftdrop"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":LiftDropDomain"))
    implementation(project(":LiftDropRepository"))

    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.jdbi:jdbi3-sqlobject:3.43.0")
    implementation("org.jdbi:jdbi3-kotlin:3.37.1")
    implementation("org.jdbi:jdbi3-postgres:3.37.1")
    implementation("org.postgresql:postgresql:42.7.2")

    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.0")

    implementation("ch.qos.logback:logback-classic:1.4.14")

    testImplementation(project(":LiftDropDomain"))
    testImplementation(kotlin("test"))
}

tasks.test {
    workingDir = project.rootDir
    useJUnitPlatform()
    if (System.getenv("DB_URL") == null) {
        environment("DB_URL", "jdbc:postgresql://localhost:5432/liftdrop?user=postgres&password=postgres")
    }
    dependsOn("dbTestsWait")
    finalizedBy("dbTestsDown")
}

kotlin {
    jvmToolchain(21)
}

val composeFileDir: Directory by parent!!.extra
val dockerComposePath = composeFileDir.file("docker-compose.yml").toString()

task<Exec>("dbTestsUp") {
    commandLine("docker", "compose", "-f", dockerComposePath, "up", "-d", "--build", "--force-recreate", "liftdrop-for-tests")
}

task<Exec>("dbTestsWait") {
    commandLine("docker", "exec", "liftdrop-for-tests", "/app/bin/wait-for-pg.sh", "localhost")
    dependsOn("dbTestsUp")
}

task<Exec>("dbTestsDown") {
    commandLine("docker", "compose", "-f", dockerComposePath, "down", "liftdrop-for-tests")
}
