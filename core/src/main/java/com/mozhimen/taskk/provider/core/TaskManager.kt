package com.mozhimen.taskk.provider.core

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.kotlin.collections.joinT2list
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.ATaskSet
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDownload
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskOpen
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUnzip
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskVerify
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetDownload
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetInstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetOpen
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUninstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUnzip
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetVerify
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
    open fun getTaskDownloads(): List<ATaskDownload> =
        getTaskProviders().joinT2list { it.getTaskDownload() }.filterNotNull()

    open fun getTaskVerifys(): List<ATaskVerify> =
        getTaskProviders().joinT2list { it.getTaskVerify() }.filterNotNull()

    open fun getTaskUnzips(): List<ATaskUnzip> =
        getTaskProviders().joinT2list { it.getTaskUnzip() }.filterNotNull()

    open fun getTaskInstalls(): List<ATaskInstall> =
        getTaskProviders().joinT2list { it.getTaskInstall() }.filterNotNull()

    open fun getTaskOpens(): List<ATaskOpen> =
        getTaskProviders().joinT2list { it.getTaskOpen() }.filterNotNull()

    open fun getTaskUninstalls(): List<ATaskUninstall> =
        getTaskProviders().joinT2list { it.getTaskUninstall() }.filterNotNull()

    /////////////////////////////////////////////////////////////////

    override fun getTaskQueues(): Map<String, List<String>> {
        return getTaskProviders().map { provider -> (provider.getSupportFileExtensions().associateWith { provider.getTaskQueue() }).toMutableMap() }.fold(emptyMap()){ acc, nex-> acc+nex}
    }

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