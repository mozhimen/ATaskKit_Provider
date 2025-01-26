package com.mozhimen.taskk.provider.apk

import android.content.Context
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.taskk.provider.apk.impls.TaskDeleteApk
import com.mozhimen.taskk.provider.apk.impls.TaskDownloadOkDownloadApk
import com.mozhimen.taskk.provider.apk.impls.TaskOpenApk
import com.mozhimen.taskk.provider.apk.impls.TaskOpenApk2
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.bases.ATaskProvider
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskClose
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDelete
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDownload
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskOpen
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUnzip
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskVerify
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.cons.STaskNode

/**
 * @ClassName TaskProviderSetsApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
@OPermission_REQUEST_INSTALL_PACKAGES
@OApiInit_InApplication
open class TaskProviderApk2 constructor(
    iTaskLifecycle: ITaskLifecycle,
    taskManagerProvider: ATaskManagerProvider,
) : ATaskProvider(iTaskLifecycle, taskManagerProvider) {

    @OPermission_INTERNET
    override fun getTaskDownload(): ATaskDownload {
        return TaskDownloadOkDownloadApk(_taskManagerProvider,_iTaskLifecycle)
    }

    override fun getTaskVerify(): ATaskVerify? {
        return null
    }

    override fun getTaskUnzip(): ATaskUnzip? {
        return null
    }

    override fun getTaskInstall(): ATaskInstall? {
        return null
    }

    override fun getTaskOpen(): ATaskOpen {
        return TaskOpenApk2(_taskManagerProvider,_iTaskLifecycle)
    }

    override fun getTaskClose(): ATaskClose? {
        return null
    }

    override fun getTaskUninstall(): ATaskUninstall? {
        return null
    }

    override fun getTaskDelete(): ATaskDelete? {
        return TaskDeleteApk(_taskManagerProvider,_iTaskLifecycle)
    }

    ////////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_InApplication::class)
    override fun init(context: Context) {
        if (hasInit()) return
        super.init(context)
    }


    ////////////////////////////////////////////////////////////////////

    override fun getTaskNodeQueues(): Map<String, List<STaskNode>> {
        return mapOf(
            ATaskName.TASK_DOWNLOAD to listOf(STaskNode.TaskNodeDownload),
            ATaskName.TASK_OPEN to listOf(STaskNode.TaskNodeOpen),
            ATaskName.TASK_DELETE to listOf(STaskNode.TaskNodeDelete, STaskNode.TaskNodeRestart)
        )
    }
}