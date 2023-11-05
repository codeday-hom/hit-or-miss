package com.hom

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import java.io.File
import javax.inject.Inject

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
