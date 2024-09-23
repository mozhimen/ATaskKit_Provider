package com.mozhimen.taskk.provider.basic.bases.providers

import androidx.annotation.CallSuper
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATask
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
abstract class ATaskDownload(iTaskLifecycle: ITaskLifecycle?) : ATask(iTaskLifecycle) {
    protected abstract var _downloadDir: File?
    protected abstract var _taskQueueName_ofDownload: String?

    abstract fun taskResumeAll(@ATaskQueueName taskQueueName: String)
    abstract fun taskPauseAll(@ATaskQueueName taskQueueName: String)

    fun getDownloadDir(): File? =
        _downloadDir

    override fun getTaskName(): String {
        return ATaskName.TASK_DOWNLOAD
    }

    @CallSuper
    override fun taskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        onTaskStarted(ATaskState.STATE_DOWNLOADING, appTask, taskQueueName)
    }

    @CallSuper
    override fun taskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        onTaskStarted(ATaskState.STATE_DOWNLOADING, appTask, taskQueueName)
    }

    @CallSuper
    override fun taskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        onTaskPaused(ATaskState.STATE_DOWNLOAD_PAUSE, appTask, taskQueueName)
    }

    @CallSuper
    override fun taskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        onTaskFinished(ATaskState.STATE_DOWNLOAD_CANCEL, appTask, taskQueueName, STaskFinishType.CANCEL)
    }

    override fun canTaskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return true
    }

    override fun canTaskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return true
    }

    override fun canTaskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return true
    }

    override fun canTaskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return true
    }

    @CallSuper
    override fun onTaskFinished(taskState: Int, appTask: AppTask, @ATaskQueueName taskQueueName: String, finishType: STaskFinishType) {
        appTask.taskDownloadReset()
        super.onTaskFinished(taskState, appTask, taskQueueName, finishType)
    }
}