package com.example

import com.example.module.slackCommand
import com.example.module.slackViewSubmission
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.repository.DatabaseFactory
import com.example.repository.ThankRepository
import com.example.repository.UserRepository
import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.ktor.respond
import com.slack.api.bolt.ktor.toBoltRequest
import com.slack.api.bolt.util.SlackRequestParser
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun main() {

    DatabaseFactory.init()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module(testing:Boolean = false){

    val thankRepository = ThankRepository()
    val userRepository = UserRepository()

    val slackAppConfig = AppConfig()
    val slackApp = App(slackAppConfig)
    val requestParser = SlackRequestParser(slackApp.config())

    slackApp.command("/hello"){req, ctx ->
        ctx.ack("Hello, World!")
    }

    slackCommand(slackApp)
    slackViewSubmission(slackApp, thankRepository, userRepository)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/slack/events"){
            respond(call, slackApp.run(toBoltRequest(call, requestParser)))
        }
    }



}
