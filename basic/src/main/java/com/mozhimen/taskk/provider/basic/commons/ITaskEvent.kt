package com.mozhimen.taskk.provider.basic.commons

import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName ITaskProviderEvent
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskEvent {
    fun taskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String)
    fun taskResume(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String)
    fun taskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String)
    fun taskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String)

    fun canTaskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean
    fun canTaskResume(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean
    fun canTaskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean
    fun canTaskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean
}