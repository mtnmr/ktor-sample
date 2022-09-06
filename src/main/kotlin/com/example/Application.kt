package com.example

import com.example.module.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.repository.DatabaseFactory
import com.example.repository.ThankRepository
import com.example.repository.UserRepository
import com.example.route.thanksRouting
import com.example.util.Every
import com.example.util.TaskScheduler
import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.ktor.respond
import com.slack.api.bolt.ktor.toBoltRequest
import com.slack.api.bolt.util.SlackRequestParser
import freemarker.cache.ClassTemplateLoader
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.locations.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.concurrent.TimeUnit


fun main() {

    DatabaseFactory.init()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module(testing:Boolean = false){

    install(Locations)

    install(FreeMarker){
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

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
    slackReactionEvent(slackApp, thankRepository)
    slackMessageEvent(slackApp, thankRepository, userRepository)

    TaskScheduler{
        sendPostThanksMessage(thankRepository)
    }.start(
        Every(5, TimeUnit.MINUTES)
    )

    routing {
//        get("/") {
//            call.respondText("Hello World!")
//        }

        post("/slack/events"){
            respond(call, slackApp.run(toBoltRequest(call, requestParser)))
        }

        static("/static") {
            resources("css")
        }

        thanksRouting(thankRepository)
    }
}
