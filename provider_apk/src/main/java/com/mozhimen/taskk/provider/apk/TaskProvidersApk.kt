package com.mozhimen.taskk.provider.apk

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskProvider
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviders

/**
 * @ClassName TaskProviderSetsApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
class TaskProvidersApk : ITaskProviders {

    override fun getChains(): Map<String, List<String>> {
        return mapOf(
            ATaskName.TASK_DOWNLOAD to listOf(ATaskName.TASK_DOWNLOAD, ATaskName.TASK_VERIFY, ATaskName.TASK_UNZIP, ATaskName.TASK_INSTALL),
            ATaskName.TASK_OPEN to listOf(ATaskName.TASK_OPEN),
            ATaskName.TASK_DOWNLOAD to listOf(ATaskName.TASK_UNINSTALL)
        )
    }

    override fun getProviders(): List<ATaskProvider> {
        return listOf(

        )
    }
}