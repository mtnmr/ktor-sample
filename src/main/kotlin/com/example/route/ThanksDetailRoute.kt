package com.example.route

import com.example.model.Thank
import com.example.model.ThankReaction
import com.example.repository.ThankRepository
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.locations.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Location("/thanks/{thankId}")
class ThanksDetailRoute(val thankId:Int)

fun Route.thanksDetailRouting(thankRepository: ThankRepository){
    get<ThanksDetailRoute>{ listing ->
        val thank = thankRepository.getThank(listing.thankId)
        val reactions:List<ThankReaction>
        val threads:List<Thank>

        if(thank.slackPostId == null){
            reactions = emptyList()
            threads = emptyList()
        }else{
            reactions = thankRepository.getReactions(thank.slackPostId)
            threads = thankRepository.getThreads(thank.slackPostId)
        }

        call.respond(
            FreeMarkerContent(
                "thanks_detail.ftl",
                mapOf(
                    "thank" to thank,
                    "reactions" to reactions,
                    "threads" to threads
                )
            )
        )
    }
}