package com.mozhimen.taskk.provider.basic.bases.sets

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskProviderSet
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderDownload

/**
 * @ClassName ITaskPRoviderSetDownload
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
abstract class ATaskProviderSetDownload : ATaskProviderSet<ATaskProviderDownload>() {
    override fun getTaskName(): String {
        return ATaskName.TASK_DOWNLOAD
    }

    abstract fun taskPauseAll()
    abstract fun taskResumeAll()
}