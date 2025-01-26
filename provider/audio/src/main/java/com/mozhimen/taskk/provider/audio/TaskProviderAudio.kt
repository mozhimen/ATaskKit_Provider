package com.mozhimen.taskk.provider.audio

import android.content.Context
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.taskk.provider.audio.impls.TaskDeleteAudio
import com.mozhimen.taskk.provider.audio.impls.TaskDownloadOkDownloadAudio
import com.mozhimen.taskk.provider.audio.impls.TaskOpenAudio
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
@OApiInit_InApplication
open class TaskProviderAudio(
    iTaskLifecycle: ITaskLifecycle,
    taskManagerProvider: ATaskManagerProvider,
) : ATaskProvider(iTaskLifecycle, taskManagerProvider) {

    @OPermission_INTERNET
    override fun getTaskDownload(): ATaskDownload {
        return TaskDownloadOkDownloadAudio(_taskManagerProvider,_iTaskLifecycle)
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
        return TaskOpenAudio(_taskManagerProvider,_iTaskLifecycle)
    }

    override fun getTaskClose(): ATaskClose? {
        return null
    }

    override fun getTaskUninstall(): ATaskUninstall? {
        return null
    }

    override fun getTaskDelete(): ATaskDelete? {
        return TaskDeleteAudio(_taskManagerProvider,_iTaskLifecycle)
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