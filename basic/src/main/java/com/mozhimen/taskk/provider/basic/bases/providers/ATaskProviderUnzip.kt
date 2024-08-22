package com.mozhimen.taskk.provider.basic.bases.providers

import androidx.annotation.CallSuper
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskProvider
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderLifecycle
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @ClassName INetKAppUnzipProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
abstract class ATaskProviderUnzip(iTaskProviderLifecycle: ITaskProviderLifecycle?) : ATaskProvider(iTaskProviderLifecycle) {
    protected abstract var _unzipDir: File?

    @Volatile
    protected var _sniffTargetFile: String = ""

    abstract fun getIgnorePaths(): List<String>

    override fun getTaskName(): String {
        return ATaskName.TASK_UNZIP
    }

    @CallSuper
    override fun taskStart(appTask: AppTask) {
        onTaskStarted(CTaskState.STATE_UNZIPING, appTask)
    }

    @CallSuper
    override fun taskResume(appTask: AppTask) {
        onTaskStarted(CTaskState.STATE_UNZIPING, appTask)
    }

    @CallSuper
    override fun taskPause(appTask: AppTask) {
        onTaskPaused(CTaskState.STATE_UNZIP_PAUSE, appTask)
    }

    @CallSuper
    override fun taskCancel(appTask: AppTask) {
        onTaskFinished(CTaskState.STATE_UNZIP_CANCEL, STaskFinishType.CANCEL, appTask)
    }
}