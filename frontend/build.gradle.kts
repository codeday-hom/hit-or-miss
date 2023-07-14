import com.github.gradle.node.yarn.task.YarnTask

plugins {
    id("com.github.node-gradle.node") version "5.0.0"
}

node {
    download.set(true)
    version.set("20.4.0")
}

val reactOutputDir = "build"
val createBuildDir by tasks.registering {
    doFirst {
        mkdir(reactOutputDir)
    }
    outputs.dir(reactOutputDir)
}

val yarnInstall by tasks.registering(YarnTask::class) {
    dependsOn("npmSetup")
    inputs.file("package.json")
    outputs.dir("node_modules")
    outputs.file("yarn.lock")
    args.set(listOf("install"))
}

val yarnBuild by tasks.registering(YarnTask::class) {
    dependsOn(yarnInstall)
    outputs.upToDateWhen { false }
    inputs.files(fileTree("node_modules"))
    inputs.files(fileTree("src"))
    inputs.files("public")
    inputs.file("package.json")
    outputs.dir(reactOutputDir)
    args.set(listOf("build"))
}

tasks.register<YarnTask>("yarnTest") {
    inputs.dir(reactOutputDir)
    dependsOn(yarnInstall)
    args.set(listOf("test"))
}
