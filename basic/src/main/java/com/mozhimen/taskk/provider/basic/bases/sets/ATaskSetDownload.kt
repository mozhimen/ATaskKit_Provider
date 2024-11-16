package com.mozhimen.taskk.provider.basic.bases.sets

import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.ATaskSet
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDownload
import java.io.File

/**
 * @ClassName ITaskPRoviderSetDownload
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
@OPermission_INTERNET
abstract class ATaskSetDownload(taskManager: ATaskManager) : ATaskSet<ATaskDownload>(taskManager) {
    fun getDownloadDirs(): List<File> =
        providers.values.mapNotNull { it.getDownloadDir() }.toSet().toList()

    fun getDownloadPaths(): List<String> =
        getDownloadDirs().map { it.absolutePath }

    fun taskPauseAll(fileExt: String, @ATaskNodeQueueName taskNodeQueueName: String) {
        getProvider(fileExt)?.taskPauseAll(taskNodeQueueName)
    }

    fun taskResumeAll(fileExt: String, @ATaskNodeQueueName taskNodeQueueName: String) {
        getProvider(fileExt)?.taskResumeAll(taskNodeQueueName)
    }

    override fun getTaskName(): String {
        return ATaskName.TASK_DOWNLOAD
    }
}