package com.mozhimen.taskk.provider.basic.interfaces

import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName ITaskProviderEvent
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskEvent {
    fun taskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String)
    fun taskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String)
    fun taskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String)
    fun taskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String)

    fun canTaskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean
    fun canTaskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean
    fun canTaskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean
    fun canTaskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean
}