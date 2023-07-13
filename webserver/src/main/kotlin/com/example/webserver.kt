package com.example

import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.io.File
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes


class webserver {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val server: Http4kServer = routes(
                "/" bind Method.GET to { _: org.http4k.core.Request ->
                    val file = File("./webserver.html")
                    if (file.exists()) {
                        val htmlContent = file.readText()
                        Response(OK).body(htmlContent)
                    } else {
                        Response(NOT_FOUND)
                    }
                }
            ).asServer(Jetty(8080)).start()

            println("Server started on port ${server.port()}")
            readLine()

            server.stop()
        }
    }

    plugins
    {
        id("application")
        kotlin("jvm") version "1.8.22"
    }

/*    application
    {
        // `./gradlew run` won't work because this class doesn't exist, as I haven't written it!
        mainClass.set("com.example.webserverKt")
    }*/
}


