package com.example.model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.jodatime.datetime
import org.jetbrains.exposed.sql.select
import org.joda.time.DateTime
import java.io.Serializable

data class User(
    val id: Int,
    val slackUserId: String,
    val realName: String,
    val userImage:String,
    val createdAt: DateTime,
    val updatedAt: DateTime
): Serializable


//Exposedを使ってデータベースのテーブル定義
object UsersTable: IntIdTable(name="users"){
    val slackUserID = varchar(name="slack_user_id", length=255)
    val realName = varchar(name="real_name", length=255)
    val userImage = varchar(name="user_image", length=255)
    val createdAt = datetime(name="created_at").default(DateTime.now())
    val updatedAt = datetime(name="updated_at").default(DateTime.now())

    //slackUserIdからUserを取得する
    fun getUserBySlackUserId(slackUserId: String): User{
        return UsersTable.select{UsersTable.slackUserID eq slackUserId }.map {
            toUser(it)
        }.single()
    }

    //Exposedの戻り値ResultRowをUserデータクラスに変換
    fun toUser(row:ResultRow): User{
        return User(
            id= row[id].value,
            slackUserId = row[slackUserID],
            realName = row[realName],
            userImage = row[userImage],
            createdAt = row[createdAt],
            updatedAt = row[updatedAt]
        )
    }
}