import java.io.IOException

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

    val dockerImport by registering(DockerTask::class) {
        args.add("import")
        args.add(dockerBuildAndSave.flatMap { task -> task.outputTar.map { it.asFile.absolutePath } })
        args.add(dockerBuildAndSave.flatMap { it.imageName })
    }

    val dockerRun by registering(DockerTask::class) {
        dependsOn(dockerBuildAndSave)
        args.set(listOf("run", "-p", "8080:8080", "--name", "hit-or-miss"))
        args.add(dockerBuildAndSave.flatMap { it.imageName })
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
        logger.info("Running '${dockerCommandArgs.joinToString(" ")}'.")
        val dockerWorkingDir = workingDir
        execOps.exec {
            if (dockerWorkingDir.isPresent) {
                workingDir(dockerWorkingDir.get())
            }
            commandLine(dockerCommandArgs)
        }
    }
}

@CacheableTask
abstract class DockerBuildAndSaveTask @Inject constructor(private val execOps: ExecOperations) : DefaultTask() {

    @get:Input
    abstract val buildDir: Property<File>

    @get:Input
    abstract val imageName: Property<String>

    @get:OutputFile
    abstract val outputTar: RegularFileProperty

    @TaskAction
    fun runDockerCommands() {
        // Build
        val dockerBuildCommand = listOf("docker", "build", "-t", imageName.get(), ".")
        execOps.exec {
            workingDir(buildDir.get())
            commandLine(dockerBuildCommand)
        }
        // Save
        val outputTarFile = outputTar.get().asFile
        if (!outputTarFile.parentFile.exists() && !outputTarFile.parentFile.mkdirs()) {
            throw IOException("Couldn't create output directory: '${outputTarFile.parentFile.absolutePath}'.")
        }
        val dockerSaveCommand = listOf("docker", "save", imageName.get(), "--output", outputTarFile.absolutePath)
        execOps.exec {
            commandLine(dockerSaveCommand)
        }
    }
}
