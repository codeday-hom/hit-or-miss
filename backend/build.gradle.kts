import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.test {
    useJUnitPlatform()
}

val frontendBuild by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:5.3.0.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-jetty")
    implementation("org.http4k:http4k-client-websocket")
    implementation("org.http4k:http4k-cloudnative")
    implementation("org.http4k:http4k-server-undertow")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.slf4j:slf4j-simple:2.0.7")

    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("io.mockk:mockk:1.13.5")

    frontendBuild(project(path = ":frontend", configuration = "reactBuild"))
}

application {
    mainClass.set("com.game.main.MainKt")
}

tasks.named<JavaExec>("run") {
    inputs.files(frontendBuild)
    val commandLineArguments = providers.provider { mutableListOf("-Dreact.build.dir=${(frontendBuild as FileCollection).singleFile.absolutePath}") }
    jvmArgumentProviders += CommandLineArgumentProvider { commandLineArguments.get() }
}

val shadowJar by tasks.existing(ShadowJar::class) {
    archiveBaseName.set("hit-or-miss")
    archiveClassifier.set("")
    archiveVersion.set("")
    manifest {
        attributes.put("Main-Class", "com.game.main.MainKt")
    }
}

configurations.create("shadowJar") {
    isCanBeResolved = false
    isCanBeConsumed = true
    outgoing.artifact(shadowJar.flatMap { it.archiveFile }) {
        builtBy(shadowJar)
    }
}
