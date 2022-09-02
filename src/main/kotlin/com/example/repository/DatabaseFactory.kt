package com.example.repository

import com.example.model.ThankReactionsTable
import com.example.model.ThanksTable
import com.example.model.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(){

        //データベースへの接続
        Database.connect(hikari())

        //テーブルの作成
        transaction {
            SchemaUtils.create(ThanksTable)
            SchemaUtils.create(ThankReactionsTable)
            SchemaUtils.create(UsersTable)
        }
    }

    //JDBC(JavaDataBaseConnectivity)に関する設定、コネクションプールにHikariCpを使用
    private fun hikari():HikariDataSource{
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = System.getenv("JDBC_DATABASE_URL")
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    //データベースに接続するためのクエリ関数
    suspend fun <T> dbQuery(block:() -> T): T{
        return withContext(Dispatchers.IO){
            transaction { block() }
        }
    }
}