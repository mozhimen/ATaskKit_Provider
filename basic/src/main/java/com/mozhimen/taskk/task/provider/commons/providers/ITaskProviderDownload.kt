package com.mozhimen.taskk.task.provider.commons.providers

import androidx.annotation.CallSuper
import com.mozhimen.taskk.task.provider.annors.ATaskName
import com.mozhimen.taskk.task.provider.commons.ITaskProvider
import com.mozhimen.taskk.task.provider.cons.STaskFinishType
import com.mozhimen.taskk.task.provider.db.AppTask
import java.io.File

/**
 * @ClassName ITaskProviderDownload
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskProviderDownload : ITaskProvider {
    var _downloadDir: File?

    override fun getTaskName(): String {
        return ATaskName.TASK_DOWNLOAD
    }

    @CallSuper
    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        super.onTaskFinished(taskState, finishType, appTask)
        appTask.taskDownloadReset()
    }
}