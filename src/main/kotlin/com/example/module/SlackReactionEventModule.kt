package com.example.module

import com.example.repository.ThankRepository
import com.slack.api.bolt.App
import com.slack.api.model.event.ReactionAddedEvent
import com.slack.api.model.event.ReactionRemovedEvent
import io.ktor.server.application.*
import kotlinx.coroutines.launch


fun Application.slackReactionEvent(
    app: App,
    thankRepository: ThankRepository
){

    app.event(ReactionAddedEvent::class.java){ payload, ctx ->
        val event = payload.event

        if(event.item.channel == System.getenv("SLACK_THANKS_CHANNEL")){
            launch {
                thankRepository.createReaction(event)
            }
        }

        ctx.ack()
    }

    app.event(ReactionRemovedEvent::class.java){ payload, ctx ->
        val event = payload.event

        if(event.item.channel == System.getenv("SLACK_THANKS_CHANNEL")){
            launch {
                thankRepository.removeReaction(event)
            }
        }

        ctx.ack()
    }
}