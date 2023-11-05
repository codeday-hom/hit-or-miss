package com.hom

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Combines build and save into a single task in order to make it cacheable by having
 * clear input files (the build directory) and a clear output file (the saved image tarball).
 */
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