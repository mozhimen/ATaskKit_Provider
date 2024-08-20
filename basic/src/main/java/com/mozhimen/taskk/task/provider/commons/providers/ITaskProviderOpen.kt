package com.mozhimen.taskk.task.provider.commons.providers

import com.mozhimen.taskk.task.provider.annors.ATaskName
import com.mozhimen.taskk.task.provider.commons.ITaskProvider

/**
 * @ClassName ITaskProviderOpen
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskProviderOpen:ITaskProvider {
    override fun getTaskName(): String {
        return ATaskName.TASK_OPEN
    }
}