tasks.register<Exec>("buildReactApp") {
    doFirst {
        logger.lifecycle("Success on buildReactApp!")
    }
    outputs.dir("build")
    workingDir = project.file("../frontend")
    commandLine("cmd.exe", "/C", "npm.cmd run build")
}