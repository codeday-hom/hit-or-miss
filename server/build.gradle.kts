plugins {
    application
}

application {
    mainClass.set("org.gradle.sample.Main") // placeholder
}

tasks.named("run") {
    dependsOn(":frontend:buildReactApp")
}
