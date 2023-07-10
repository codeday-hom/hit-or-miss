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
