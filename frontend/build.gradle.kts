import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("com.github.node-gradle.node") version "5.0.0"
}

tasks.register<NpmTask>("buildReactApp") {
    args.set(listOf("run", "build"))
}