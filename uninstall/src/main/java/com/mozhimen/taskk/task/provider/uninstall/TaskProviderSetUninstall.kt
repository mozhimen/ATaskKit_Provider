package com.mozhimen.taskk.task.provider.uninstall

import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderUninstall
import com.mozhimen.taskk.task.provider.commons.sets.ITaskProviderSetUninstall
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskProvidersUninstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
class TaskProviderSetUninstall(override val providerDefault: ITaskProviderUninstall) : ITaskProviderSetUninstall {
    override val providers: ConcurrentHashMap<String, ITaskProviderUninstall> by lazy { ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault }) }
}