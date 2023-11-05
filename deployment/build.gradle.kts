import com.hom.DockerBuildAndSaveTask
import com.hom.DockerTask

plugins {
    base
    id("com.palantir.git-version") version "3.0.0"
    id("hom.docker")
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

    val dockerRun by registering(DockerTask::class) {
        dependsOn(dockerBuildAndSave)
        args.set(listOf("run", "-p", "8080:8080", "--name", "hit-or-miss"))
        args.add(dockerBuildAndSave.flatMap { it.imageName })
    }
}
