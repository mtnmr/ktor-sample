package com.example.module

import com.slack.api.bolt.App
import com.slack.api.bolt.response.Response
import com.slack.api.model.kotlin_extension.view.blocks
import com.slack.api.model.view.View
import com.slack.api.model.view.Views
import io.ktor.server.application.*


fun Application.slackCommand(app: App) {

    //Boltアプリは SlackAPIサーバーからのリクエストに対して3秒以内にack()メソッドで応答する必要がある

    app.command("/thanks") { _, ctx ->  //第二引数はcontext
        //モーダルウィンドウを作成する
        val res = ctx.client().viewsOpen {
            it.triggerId(ctx.triggerId)  //トリガーIDを指定
            it.view(  //viewの作成
                Views.view { thisView -> thisView
                    .callbackId("thanks-message")
                    .type("modal") //モーダルでダイアログを表示
                    .notifyOnClose(true)  //モーダルを閉じた時に通知を受け取る
                    .title(Views.viewTitle { title ->
                        //タイトル指定
                        title.type("plain_text")
                            .text("あなたのありがとうを教えて！")
                            .emoji(true)
                    })
                    .submit(Views.viewSubmit { submit ->
                        //実行ボタン指定
                        submit.type("plain_text")
                            .text("送信")
                            .emoji(true)
                    })
                    .close(Views.viewClose { close ->
                        //閉じるボタン指定
                        close.type("plain_text")
                            .text("キャンセル")
                            .emoji(true)
                    })
                    .blocks {
                        //相手を指定するための入力項目
                        input {
                            blockId("user-block")
                            label(text="誰に届けますか？", emoji = true)
                            element {
                                multiUsersSelect {
                                    actionId("user-action")
                                    placeholder("選択してみよう")
                                }
                            }
                        }

                        //送るメッセージの入力
                        input{
                            blockId("message-block")
                            element {
                                plainTextInput {
                                    actionId("message-action")
                                    multiline(true)
                                }
                            }
                            label(text="メッセージをどうぞ", emoji = true)
                        }
                    }

                }
            )
        }

        if(res.isOk){
            ctx.ack()
        }else{
            Response.builder().statusCode(500).body(res.error).build()
        }

    }
}