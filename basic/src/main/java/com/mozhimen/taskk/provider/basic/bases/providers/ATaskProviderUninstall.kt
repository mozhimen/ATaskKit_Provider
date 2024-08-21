package com.mozhimen.taskk.provider.basic.bases.providers

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskProvider
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderLifecycle

/**
 * @ClassName INetKAppUnzipProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
abstract class ATaskProviderUninstall(iTaskProviderLifecycle: ITaskProviderLifecycle?) : ATaskProvider(iTaskProviderLifecycle) {
    override fun getTaskName(): String {
        return ATaskName.TASK_UNINSTALL
    }
}