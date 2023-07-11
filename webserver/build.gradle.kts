plugins {
    application
    kotlin("jvm") version "1.9.0"
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.gradle.sample.Main") // placeholder
}

tasks.named("run") {
    dependsOn(":frontend:buildReactApp")
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    val http4kVersion = "5.3.0.0"
    val junitVersion = "5.9.0"
    val kotlinVersion = "1.9.0"
    implementation("org.http4k:http4k-core:${http4kVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    testImplementation("org.http4k:http4k-testing-approval:${http4kVersion}")
    testImplementation("org.http4k:http4k-testing-hamkrest:${http4kVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

