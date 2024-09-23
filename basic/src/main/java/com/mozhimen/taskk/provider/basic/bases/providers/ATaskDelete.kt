package com.mozhimen.taskk.provider.basic.bases.providers

import android.annotation.SuppressLint
import androidx.annotation.CallSuper
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.cons.CState
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle

/**
 * @ClassName INetKAppUnzipProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
abstract class ATaskDelete(iTaskLifecycle: ITaskLifecycle?) : ATask(iTaskLifecycle) {
    override fun getTaskName(): String {
        return ATaskName.TASK_DELETE
    }

    @CallSuper
    override fun taskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        onTaskStarted(CTaskState.STATE_DELETING, appTask)
    }

    override fun taskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
    }

    override fun taskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
    }

    override fun taskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
    }

    override fun canTaskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return true
    }

    override fun canTaskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return false
    }

    override fun canTaskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return false
    }

    override fun canTaskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return false
    }

    @SuppressLint("MissingSuperCall")
    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        if (finishType is STaskFinishType.SUCCESS) {
            val taskStateNew = CState.STATE_TASK_CREATE
            appTask.toNewTaskState(taskStateNew)
            _iTaskLifecycle?.onTaskFinished(taskStateNew, STaskFinishType.SUCCESS, appTask)
        }
    }
}