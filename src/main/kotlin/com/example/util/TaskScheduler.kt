package com.example.util

import kotlinx.coroutines.Runnable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

//定期実行のスケジューラー
class TaskScheduler(private val task: Runnable) {
    //newScheduledThreadPool:指定された遅延時間後、または周期的にコマンドの実行をスケジュールできる、スレッド・プールを作成
    private val executor = Executors.newScheduledThreadPool(1)

    fun start(every:Every){
        //Runnableは別スレッドの作成
        val task = Runnable {
            task.run()
        }

        executor.scheduleWithFixedDelay(task, every.next, every.next, every.unit)
    }

    fun stop(){
        executor.shutdown()

        try{
            executor.awaitTermination(1, TimeUnit.HOURS)
        }catch (e:InterruptedException){

        }
    }
}

//定期実行の時間に関するクラス
data class Every(val next:Long, val unit: TimeUnit)