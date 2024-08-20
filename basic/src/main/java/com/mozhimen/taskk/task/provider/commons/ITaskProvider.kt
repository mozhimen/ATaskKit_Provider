package com.mozhimen.taskk.task.provider.commons

import androidx.annotation.CallSuper
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.taskk.task.provider.cons.STaskFinishType
import com.mozhimen.taskk.task.provider.db.AppTask

/**
 * @ClassName INetKAppProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
interface ITaskProvider : IUtilK, ITaskProviderLifecycle, ITaskProviderEvent {
    fun getTaskName(): String
    fun getSupportFileExtensions(): List<String>

    @CallSuper
    override fun onTaskPaused(taskState: Int, appTask: AppTask) {
        appTask.taskState = taskState
    }

    @CallSuper
    override fun onTaskStarted(taskState: Int, appTask: AppTask) {
        appTask.taskState = taskState
    }

    @CallSuper
    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        appTask.taskState = taskState
    }
}