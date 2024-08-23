package com.mozhimen.taskk.provider.core

import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.ATaskSet
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDownload
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskOpen
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUnzip
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskVerify
import com.mozhimen.taskk.provider.download.TaskSetDownload
import com.mozhimen.taskk.provider.install.TaskSetInstall
import com.mozhimen.taskk.provider.open.TaskSetOpen
import com.mozhimen.taskk.provider.uninstall.TaskSetUninstall
import com.mozhimen.taskk.provider.unzip.TaskSetUnzip
import com.mozhimen.taskk.provider.verify.TaskSetVerify

/**
 * @ClassName TaskProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/23
 * @Version 1.0
 */
@OApiInit_InApplication
@OPermission_INTERNET
abstract class TaskManager : ATaskManager() {
    abstract fun getTaskDownloads(): List<ATaskDownload>

    abstract fun getTaskVerifys(): List<ATaskVerify>

    abstract fun getTaskUnzips(): List<ATaskUnzip>

    abstract fun getTaskInstalls(): List<ATaskInstall>

    abstract fun getTaskOpens(): List<ATaskOpen>

    abstract fun getTaskUninstalls(): List<ATaskUninstall>

    override fun getTaskSets(): List<ATaskSet<*>> {
        return listOf(
            TaskSetDownload(getTaskDownloads()),
            TaskSetVerify(getTaskVerifys()),
            TaskSetUnzip(getTaskUnzips()),
            TaskSetInstall(getTaskInstalls()),
            TaskSetOpen(getTaskOpens()),
            TaskSetUninstall(getTaskUninstalls())
        )
    }
}