package com.mozhimen.taskk.task.provider.commons

import com.mozhimen.taskk.task.provider.db.AppTask

/**
 * @ClassName ITaskProviderProcess
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskProviderLifecycle {
    fun onTaskStarted(taskState:Int, appTask: AppTask)
    fun onTaskPaused(taskState:Int, appTask: AppTask)
    fun onTaskCanceled(taskState:Int, appTask: AppTask)
    fun onTaskSucceeded(taskState:Int, appTask: AppTask)
    fun onTaskFailed(taskState:Int, appTask: AppTask)
}