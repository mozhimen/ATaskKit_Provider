package com.mozhimen.taskk.provider.basic.bases.sets

import com.mozhimen.basick.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.taskk.provider.basic.annors.ATaskName
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
abstract class ATaskSetDownload : ATaskSet<ATaskDownload>() {
    fun getDownloadDirs(): List<File> =
        providers.values.mapNotNull { it.getDownloadDir() }.toSet().toList()

    fun getDownloadPaths(): List<String> =
        getDownloadDirs().map { it.absolutePath }

    override fun getTaskName(): String {
        return ATaskName.TASK_DOWNLOAD
    }

    abstract fun taskPauseAll()
    abstract fun taskResumeAll()
}