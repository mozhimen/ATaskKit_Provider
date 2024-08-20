package com.mozhimen.taskk.task.provider.commons.sets

import com.mozhimen.taskk.task.provider.annors.ATaskName
import com.mozhimen.taskk.task.provider.commons.ITaskProviderSet
import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderUninstall

/**
 * @ClassName ITaskProviderSetUninstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskProviderSetUninstall : ITaskProviderSet<ITaskProviderUninstall> {
    override fun getTaskName(): String {
        return ATaskName.TASK_UNINSTALL
    }
}