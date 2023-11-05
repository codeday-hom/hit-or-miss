import com.hom.DockerBuildAndSaveTask
import com.hom.DockerTask
import com.hom.FlyTask

plugins {
    base
    id("com.palantir.git-version") version "3.0.0"
    id("hom.deploy")
}

val frontendBuild by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val backendShadowJar by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    frontendBuild(project(path = ":frontend", configuration = "reactBuild"))
    backendShadowJar(project(path = ":backend", configuration = "shadowJar"))
}

tasks {
    val dockerDirBuild by registering(Copy::class) {
        from("src/docker")
        from(backendShadowJar)
        into(layout.buildDirectory.dir(name))

        from(frontendBuild) {
            into("assets")
        }

        doFirst {
            destinationDir.deleteRecursively()
        }
    }

    val gitVersion: groovy.lang.Closure<String> by extra
    val dockerBuildAndSave by registering(DockerBuildAndSaveTask::class) {
        val dockerImageName = "hit-or-miss:${gitVersion().replace(".", "-")}"
        buildDir.set(dockerDirBuild.map { it.destinationDir })
        imageName.set(dockerImageName)
        outputTar.set(layout.buildDirectory.file("$name/${dockerImageName.replace(":", "--")}.tar"))
    }

    val builtImageName = dockerBuildAndSave.flatMap { it.imageName }
    val flyImageName = builtImageName.map { "registry.fly.io/$it" }

    val dockerRun by registering(DockerTask::class) {
        dependsOn(dockerBuildAndSave)
        args.set(listOf("run", "-p", "8080:8080", "--name", "hit-or-miss"))
        args.add(builtImageName)
    }

    val dockerTag by registering(DockerTask::class) {
        dependsOn(dockerBuildAndSave)
        args.set(listOf("tag"))
        args.add(builtImageName)
        args.add(flyImageName)
    }

    val dockerPush by registering(DockerTask::class) {
        dependsOn(dockerTag)
        args.set(listOf("push"))
        args.add(flyImageName)
    }

    val flyDeploy by registering(FlyTask::class) {
        dependsOn(dockerPush)
        args.set(listOf("deploy", "--app", "hit-or-miss", "--image"))
        args.add(flyImageName)
        args.addAll(listOf("--local-only", "--now", "--yes"))
    }
}
