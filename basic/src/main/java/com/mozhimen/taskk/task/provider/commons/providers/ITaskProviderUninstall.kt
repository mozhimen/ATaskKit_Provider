package com.mozhimen.taskk.task.provider.commons.providers

import com.mozhimen.taskk.task.provider.annors.ATaskName
import com.mozhimen.taskk.task.provider.commons.ITaskProvider

/**
 * @ClassName INetKAppUnzipProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
interface ITaskProviderUninstall : ITaskProvider{
    override fun getTaskName(): String {
        return ATaskName.TASK_UNINSTALL
    }
}