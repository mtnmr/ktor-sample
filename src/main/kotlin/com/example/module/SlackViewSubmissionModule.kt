package com.example.module

import com.example.model.ThankRequest
import com.example.model.UserRequest
import com.example.repository.ThankRepository
import com.example.repository.UserRepository
import com.slack.api.bolt.App
import io.ktor.server.application.*
import kotlinx.coroutines.launch

//thanksのモーダルの入力結果を受け取る
fun Application.slackViewSubmission(
    app: App,
    thankRepository: ThankRepository,
    userRepository: UserRepository
){

    //サンクスモーダルで定義したcallbackIdをセットする
    app.viewSubmission("thanks-message"){ req, ctx ->
        val stateValues = req.payload.view.state.values
        val message = stateValues["message-block"]?.get("message-action")?.value
        val targetUsers = stateValues["user-block"]?.get("user-action")?.selectedUsers

        if (message?.isNotEmpty() == true && targetUsers?.isNotEmpty() == true){
            val slackUserId = req.payload.user.id

            launch {
                listOf(*targetUsers.toTypedArray()).forEach { targetSlackUserId ->
                    thankRepository.createThank(
                        ThankRequest(
                            slackUserId = slackUserId,
                            targetSlackUserId = targetSlackUserId,
                            body = message
                        )
                    )
                }

                listOf(*targetUsers.toTypedArray(), slackUserId).distinct().forEach {targetUserId ->
                    if(userRepository.getUser(targetUserId) == null){
                        val slackUsersInfo = userRepository.getSlackUsersInfo(targetUserId)
                        userRepository.createUser(
                            UserRequest(
                                slackUserId = targetUserId,
                                realName = slackUsersInfo.user.realName,
                                userImage = slackUsersInfo.user.profile.image512
                            )
                        )
                    }
                }
            }
        }

        //自分にのみメッセージが送信されたことを通知する
        ctx.client().chatPostEphemeral {
            it.token(ctx.botToken)
            it.user(req.payload.user.id)
            it.channel("#general")
            it.text("メッセージが送信されました")
        }

        ctx.ack()
    }
}