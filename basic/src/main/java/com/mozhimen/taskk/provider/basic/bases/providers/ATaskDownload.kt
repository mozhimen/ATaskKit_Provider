package com.mozhimen.taskk.provider.basic.bases.providers

import androidx.annotation.CallSuper
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import java.io.File

/**
 * @ClassName ITaskProviderDownload
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
@OPermission_INTERNET
abstract class ATaskDownload(taskManager: ATaskManager,iTaskLifecycle: ITaskLifecycle?) : ATask(taskManager,iTaskLifecycle) {
    protected abstract var _downloadDir: File?
    protected abstract var _taskNodeQueueName_ofDownload: String?

    abstract fun taskResumeAll(@ATaskNodeQueueName taskNodeQueueName: String)
    abstract fun taskPauseAll(@ATaskNodeQueueName taskNodeQueueName: String)

    fun getDownloadDir(): File? =
        _downloadDir

    override fun getTaskName(): String {
        return ATaskName.TASK_DOWNLOAD
    }

    @CallSuper
    override fun taskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        onTaskStarted(ATaskState.STATE_DOWNLOADING, appTask, taskNodeQueueName)
    }

    @CallSuper
    override fun taskResume(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        onTaskStarted(ATaskState.STATE_DOWNLOADING, appTask, taskNodeQueueName)
    }

    @CallSuper
    override fun taskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        onTaskPaused(ATaskState.STATE_DOWNLOAD_PAUSE, appTask, taskNodeQueueName)
    }

    @CallSuper
    override fun taskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        onTaskFinished(ATaskState.STATE_DOWNLOAD_CANCEL, appTask, taskNodeQueueName, STaskFinishType.CANCEL)
    }

    override fun canTaskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return true
    }

    override fun canTaskResume(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return true
    }

    override fun canTaskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return true
    }

    override fun canTaskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return true
    }

    @CallSuper
    override fun onTaskFinished(taskState: Int, appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, finishType: STaskFinishType) {
        appTask.taskDownloadReset()
        super.onTaskFinished(taskState, appTask, taskNodeQueueName, finishType)
    }
}