package com.example.route

import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Location("/")
class HomeRoute

fun Route.homeRouting(){
    get<HomeRoute>{
        call.respondRedirect("/thanks")
    }
}