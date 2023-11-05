plugins {
    base
    id("com.palantir.git-version") version "3.0.0"
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
    val dockerDir by registering(Copy::class) {
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
    val dockerImageName = "hit-or-miss:${gitVersion().replace(".", "-")}"
    val dockerBuild by registering(DockerTask::class) {
        workingDir.set(dockerDir.map { it.destinationDir })
        args.set(listOf("build", "-t", dockerImageName, "."))
    }

    val dockerRun by registering(DockerTask::class) {
        dependsOn(dockerBuild)
        args.set(listOf("run", "-p", "8080:8080", dockerImageName))
    }
}

@DisableCachingByDefault(because = "Not all Docker tasks have a clear output")
abstract class DockerTask @Inject constructor(private val execOps: ExecOperations) : DefaultTask() {

    @get:Input
    abstract val args: ListProperty<String>

    @get:[Optional Input]
    abstract val workingDir: Property<File>

    @TaskAction
    fun runDockerCommand() {
        val dockerCommandArgs = listOf("docker") + args.get()
        val dockerWorkingDir = workingDir
        execOps.exec {
            if (dockerWorkingDir.isPresent) {
                workingDir(dockerWorkingDir.get())
            }
            commandLine(dockerCommandArgs)
        }
    }
}
