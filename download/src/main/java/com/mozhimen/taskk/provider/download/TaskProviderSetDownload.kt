package com.mozhimen.taskk.provider.download

import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderDownload
import com.mozhimen.taskk.task.provider.commons.sets.ITaskProviderSetDownload
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskProviderSetDownload
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
class TaskProviderSetDownload(override val providerDefault: ITaskProviderDownload) : ITaskProviderSetDownload {
    override val providers: ConcurrentHashMap<String, ITaskProviderDownload> by lazy { ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault }) }
}