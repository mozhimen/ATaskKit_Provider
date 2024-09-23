package com.mozhimen.taskk.provider.basic.commons

import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName ITaskProviderProcess
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskLifecycle {
    fun onTaskStarted(taskState: Int, appTask: AppTask, @ATaskQueueName taskQueueName: String)
    fun onTaskPaused(taskState: Int, appTask: AppTask, @ATaskQueueName taskQueueName: String)
    fun onTaskFinished(taskState: Int, appTask: AppTask, @ATaskQueueName taskQueueName: String, finishType: STaskFinishType)
}