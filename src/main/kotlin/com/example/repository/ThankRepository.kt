package com.example.repository

import com.example.model.*
import com.example.model.ThankReactionsTable.toThankReaction
import com.example.model.ThanksTable.toThank
import com.example.repository.DatabaseFactory.dbQuery
import com.slack.api.model.event.MessageEvent
import com.slack.api.model.event.ReactionAddedEvent
import com.slack.api.model.event.ReactionRemovedEvent
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ThankRepository {

    suspend fun createThank(thanks:ThankRequest){
        return dbQuery{
            ThanksTable.insert {
                it[slackUserId] = thanks.slackUserId
                it[body] = thanks.body
                it[targetSlackUserId] = thanks.targetSlackUserId
            }
        }
    }

    //リアクションの保存、ReactionAddEventを使ってハンドリングする
    suspend fun createReaction(event: ReactionAddedEvent){
        return dbQuery {
            ThankReactionsTable.insert {
                it[slackUserId] = event.user
                it[slackPostId] = event.item.ts
                it[reactionName] = event.reaction
            }
        }
    }

    suspend fun removeReaction(event: ReactionRemovedEvent): Boolean {
        return dbQuery {
            ThankReactionsTable.deleteWhere {
                ThankReactionsTable.slackUserId eq event.user and
                        (ThankReactionsTable.reactionName eq event.reaction) and
                        (ThankReactionsTable.slackPostId eq event.item.ts)
            } > 0
        }
    }

    //返事を保存
    suspend fun createThankReply(event: MessageEvent) {
        return dbQuery {
            ThanksTable.insert {
                it[slackUserId] = event.user
                it[body] = event.text
                it[slackPostId] = event.ts
                it[parentSlackPostId] = event.threadTs
            }
        }
    }

    //サンクス一覧の取得(ThanksRoute)
    suspend fun getThanks():List<Thank>{
        return dbQuery {
            ThanksTable.select {
                ThanksTable.parentSlackPostId.isNull()
            }.orderBy(ThanksTable.id, SortOrder.DESC).map{ toThank(it)}
        }
    }

    //サンクスの取得
    suspend fun getThank(id: Int): Thank{
        return dbQuery {
            ThanksTable.select {
                ThanksTable.id eq id
            }.map{ toThank(it) }.single()
        }
    }

    //リアクション一覧の取得
    suspend fun getReactions(slackPostId: String): List<ThankReaction> {
        return dbQuery {
            ThankReactionsTable.select {
                ThankReactionsTable.slackPostId eq slackPostId
            }.map { toThankReaction(it) }
        }
    }

    //スレッド一覧の取得
    suspend fun getThreads(slackPostId: String): List<Thank> {
        return dbQuery {
            ThanksTable.select {
                ThanksTable.parentSlackPostId eq slackPostId
            }.map { toThank(it) }
        }
    }

    //定期実行で投稿するためのサンクスを取得する
    suspend fun getPostThanks(): List<Thank>{
        return dbQuery {
            ThanksTable.select {
                ThanksTable.slackPostId.isNull()
            }.map { toThank(it) }
        }
    }

    //slackPostIdを更新
    suspend fun updateSlackPostId(ts:String, thank:Thank){
        return dbQuery {
            ThanksTable.update({
                ThanksTable.id eq thank.id
            }){
                it[slackPostId] = ts
            }
        }
    }
}
