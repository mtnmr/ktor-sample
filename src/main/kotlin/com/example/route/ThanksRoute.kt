package com.example.route

import com.example.repository.ThankRepository
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.locations.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

//Application.ktにLocationをインストール
//Locationを使ってサンクス一覧のURLを設定する
@Location("/thanks")
class ThanksRoute

fun Route.thanksRouting(thankRepository: ThankRepository){
    get<ThanksRoute>{
        val thanks = thankRepository.getThanks()

        call.respond(
            FreeMarkerContent(
                "thanks.ftl",
                mapOf(
                    "thanks" to thanks
                )
            )
        )
    }
}

