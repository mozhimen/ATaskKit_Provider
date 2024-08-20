package com.mozhimen.taskk.task.provider.commons.sets

import com.mozhimen.taskk.task.provider.annors.ATaskName
import com.mozhimen.taskk.task.provider.commons.ITaskProviderSet
import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderVerify

/**
 * @ClassName ITaskProviderSetVerify
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskProviderSetVerify : ITaskProviderSet<ITaskProviderVerify> {
    override fun getTaskName(): String {
        return ATaskName.TASK_VERIFY
    }
}