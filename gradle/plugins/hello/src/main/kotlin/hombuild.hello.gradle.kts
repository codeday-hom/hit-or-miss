tasks.register("hello") {
    doFirst {
        logger.lifecycle("Hello!")
    }
}
