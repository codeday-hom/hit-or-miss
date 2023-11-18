import com.github.gradle.node.yarn.task.YarnTask

plugins {
    id("com.github.node-gradle.node") version "5.0.0"
}

node {
    download.set(true)
    version.set("20.4.0")
}

val gitVersion by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = true
}

dependencies {
    gitVersion(project(path = ":deployment", configuration = "gitVersion"))
}

val reactOutputDir = "build"
val createBuildDir by tasks.registering {
    doFirst {
        mkdir(reactOutputDir)
    }
    outputs.dir(reactOutputDir)
}

val yarnInstall by tasks.registering(YarnTask::class) {
    args.set(listOf("install"))
    dependsOn("npmSetup")
    inputs.file("package.json")
    outputs.dir("node_modules")
    outputs.file("yarn.lock")
    outputs.upToDateWhen { false }
}

val yarnBuild by tasks.registering(YarnTask::class) {
    args.set(listOf("build"))
    dependsOn(yarnInstall)
    inputs.files(fileTree("node_modules"))
    inputs.files(fileTree("src"))
    inputs.files("public")
    inputs.file("package.json")
    inputs.files(gitVersion)
    outputs.dir(reactOutputDir)
    environment.put("REACT_APP_GIT_VERSION", providers.provider { (gitVersion as FileCollection).singleFile.readText() })
}

tasks.register<YarnTask>("yarnTest") {
    args.set(listOf("test", "--detectOpenHandles"))
    dependsOn(yarnInstall)
    inputs.dir(reactOutputDir)
}

configurations.create("reactBuild") {
    isCanBeResolved = false
    isCanBeConsumed = true
    outgoing.artifacts(providers.provider { listOf(file(reactOutputDir)) }) {
        builtBy(yarnBuild)
    }
}
