package com.mozhimen.taskk.task.provider.commons.providers

import com.mozhimen.taskk.task.provider.annors.ATaskName
import com.mozhimen.taskk.task.provider.commons.ITaskProvider

/**
 * @ClassName INetKAppInstallProvider
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
interface ITaskProviderInstall : ITaskProvider{
    override fun getTaskName(): String {
        return ATaskName.TASK_INSTALL
    }
}