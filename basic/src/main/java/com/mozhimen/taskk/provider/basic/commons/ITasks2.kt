package com.mozhimen.taskk.provider.basic.commons

import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.TaskException

/**
 * @ClassName ITasks2
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/10/1 12:43
 * @Version 1.0
 */
interface ITask2 {
    fun onTasking(appTask: AppTask, @ATaskQueueName taskQueueName: String,  progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long)
    fun onTaskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String)
    fun onTaskSuccess(appTask: AppTask, @ATaskQueueName taskQueueName: String)
    fun onTaskFail(appTask: AppTask, @ATaskQueueName taskQueueName: String, exception: TaskException)
    fun onTaskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String)
}

interface ITasks2 : ITask, ITask2