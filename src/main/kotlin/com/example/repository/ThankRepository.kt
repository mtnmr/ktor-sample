package com.example.repository

import com.example.model.ThankRequest
import com.example.model.ThanksTable
import com.example.repository.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.insert

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
}