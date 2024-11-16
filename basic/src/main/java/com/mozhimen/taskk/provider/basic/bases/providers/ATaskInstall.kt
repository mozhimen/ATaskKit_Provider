package com.mozhimen.taskk.provider.basic.bases.providers

import androidx.annotation.CallSuper
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle

/**
 * @ClassName INetKAppInstallProvider
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
abstract class ATaskInstall(taskManager: ATaskManager, iTaskLifecycle: ITaskLifecycle?) : ATask(taskManager, iTaskLifecycle) {
    override fun getTaskName(): String {
        return ATaskName.TASK_INSTALL
    }

    @CallSuper
    override fun taskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        onTaskStarted(ATaskState.STATE_INSTALLING, appTask, taskNodeQueueName)
    }

    override fun taskResume(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
    }

    override fun taskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
    }

    override fun taskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
    }

    override fun canTaskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return true
    }

    override fun canTaskResume(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return false
    }

    override fun canTaskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return false
    }

    override fun canTaskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return false
    }
}