package com.hom

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.process.ExecOperations
import javax.inject.Inject

@UntrackedTask(because = "Fly commands interact with fly's cloud services - the outputs of the task are not easy to track")
abstract class FlyTask @Inject constructor(private val execOps: ExecOperations) : DefaultTask() {

    @get:Input
    abstract val args: ListProperty<String>

    @TaskAction
    fun runFlyCommand() {
        val flyCommandArgs = listOf("fly") + args.get()
        logger.info("Running '${flyCommandArgs.joinToString(" ")}'.")
        execOps.exec {
            commandLine(flyCommandArgs)
        }
    }
}
