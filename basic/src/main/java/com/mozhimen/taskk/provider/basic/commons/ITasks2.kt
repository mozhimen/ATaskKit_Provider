package com.mozhimen.taskk.provider.basic.commons

import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
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
    fun onTasking(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long)
    fun onTaskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String)
    fun onTaskSuccess(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String)
    fun onTaskFail(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, exception: TaskException)
    fun onTaskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String)
}

interface ITasks2 : ITask, ITask2