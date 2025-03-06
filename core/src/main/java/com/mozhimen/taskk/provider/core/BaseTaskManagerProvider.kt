package com.mozhimen.taskk.provider.core

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.kotlin.collections.joinT2list
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.bases.ATaskSet
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskClose
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDelete
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDownload
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskOpen
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUnzip
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskVerify
import com.mozhimen.taskk.provider.basic.cons.STaskNode
import com.mozhimen.taskk.provider.task.close.TaskSetClose
import com.mozhimen.taskk.provider.task.delete.TaskSetDelete
import com.mozhimen.taskk.provider.task.download.TaskSetDownload
import com.mozhimen.taskk.provider.task.install.TaskSetInstall
import com.mozhimen.taskk.provider.task.open.TaskSetOpen
import com.mozhimen.taskk.provider.task.uninstall.TaskSetUninstall
import com.mozhimen.taskk.provider.task.unzip.TaskSetUnzip
import com.mozhimen.taskk.provider.task.verify.TaskSetVerify

/**
 * @ClassName TaskProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/23
 * @Version 1.0
 */
@OApiInit_InApplication
abstract class BaseTaskManagerProvider(channel:String) : ATaskManagerProvider(channel) {
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

    open fun getTaskCloses(): List<ATaskClose> =
        getTaskProviders().joinT2list { it.getTaskClose() }.filterNotNull()

    open fun getTaskUninstalls(): List<ATaskUninstall> =
        getTaskProviders().joinT2list { it.getTaskUninstall() }.filterNotNull()

    open fun getTaskDeletes(): List<ATaskDelete> =
        getTaskProviders().joinT2list { it.getTaskDelete() }.filterNotNull()

    /////////////////////////////////////////////////////////////////

    override fun getTaskNodeQueues(): Map<String, Map<String, List<STaskNode>>> {
        return getTaskProviders().map { provider -> (provider.getSupportFileExtensions().associateWith { provider.getTaskNodeQueues() }) }.reduce { acc, nex -> acc + nex }
    }

    override fun getTaskSets(): List<ATaskSet<*>> {
        return listOf(
            TaskSetDownload(this,getTaskDownloads()),
            TaskSetVerify(this,getTaskVerifys()),
            TaskSetUnzip(this,getTaskUnzips()),
            TaskSetInstall(this,getTaskInstalls()),
            TaskSetOpen(this,getTaskOpens()),
            TaskSetClose(this,getTaskCloses()),
            TaskSetUninstall(this,getTaskUninstalls()),
            TaskSetDelete(this,getTaskDeletes())
        )
    }
}
