package com.mozhimen.taskk.provider.basic.bases.providers

import androidx.annotation.CallSuper
import com.mozhimen.basick.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle
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

    abstract fun taskResumeAll()
    abstract fun taskPauseAll()

    fun getDownloadDir(): File? =
        _downloadDir

    override fun getTaskName(): String {
        return ATaskName.TASK_DOWNLOAD
    }

    @CallSuper
    override fun taskStart(appTask: AppTask) {
        onTaskStarted(CTaskState.STATE_DOWNLOADING, appTask)
    }

    @CallSuper
    override fun taskResume(appTask: AppTask) {
        onTaskStarted(CTaskState.STATE_DOWNLOADING, appTask)
    }

    @CallSuper
    override fun taskPause(appTask: AppTask) {
        onTaskPaused(CTaskState.STATE_DOWNLOAD_PAUSE, appTask)
    }

    @CallSuper
    override fun taskCancel(appTask: AppTask) {
        onTaskFinished(CTaskState.STATE_DOWNLOAD_CANCEL, STaskFinishType.CANCEL, appTask)
    }

    @CallSuper
    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        appTask.taskDownloadReset()
        super.onTaskFinished(taskState, finishType, appTask)
    }
}