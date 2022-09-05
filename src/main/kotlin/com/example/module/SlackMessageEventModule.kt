package com.example.module

import com.example.model.UserRequest
import com.example.repository.ThankRepository
import com.example.repository.UserRepository
import com.slack.api.bolt.App
import com.slack.api.model.event.MessageEvent
import io.ktor.server.application.*
import kotlinx.coroutines.launch

fun Application.slackMessageEvent(
    app:App,
    thankRepository: ThankRepository,
    userRepository: UserRepository
){

    app.event(MessageEvent::class.java){ payload, ctx ->
        val event = payload.event

        if(event.channel == System.getenv("SLACK_THANKS_CHANNEL")){
            launch {
                thankRepository.createThankReply(event)

                if(userRepository.getUser(event.user) == null){
                    val slackUsersInfo = userRepository.getSlackUsersInfo(event.user)

                    userRepository.createUser(
                        UserRequest(
                            slackUserId = event.user,
                            realName = slackUsersInfo.user.realName,
                            userImage = slackUsersInfo.user.profile.image512
                        )
                    )
                }
            }
        }

        ctx.ack()
    }
}