package com.example.repository

import com.example.model.User
import com.example.model.UserRequest
import com.example.model.UsersTable
import com.example.repository.DatabaseFactory.dbQuery
import com.slack.api.Slack
import com.slack.api.methods.request.users.UsersInfoRequest
import com.slack.api.methods.response.users.UsersInfoResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class UserRepository {

    //ユーザーの保存
    suspend fun createUser(request:UserRequest){
        return dbQuery{
            UsersTable.insert {
                it[slackUserID] = request.slackUserId
                it[realName] = request.realName
                it[userImage] = request.userImage
            }
        }
    }

    //ユーザーの取得
    suspend fun getUser(slackUserId:String): User?{
        return dbQuery {
            UsersTable.select {
                UsersTable.slackUserID eq slackUserId
            }.map { UsersTable.toUser(it) }.singleOrNull()
        }
    }

    //slackユーザー情報を取得
    suspend fun getSlackUsersInfo(slackUserId: String):UsersInfoResponse{
        val slack = Slack.getInstance()
        val apiClient = slack.methods(System.getenv("SLACK_BOT_TOKEN"))

        val request = UsersInfoRequest
            .builder()
            .token(System.getenv("SLACK_BOT_TOKEN"))
            .user(slackUserId)
            .build()

        return dbQuery {
            apiClient.usersInfo(request)
        }
    }
}