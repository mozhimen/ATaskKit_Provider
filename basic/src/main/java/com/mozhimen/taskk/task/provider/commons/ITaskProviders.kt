package com.mozhimen.taskk.task.provider.commons

import com.mozhimen.taskk.task.provider.db.AppTask
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName ITaskProviders
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
interface ITaskProviders<T : ITaskProvider> : ITaskProvider {
    val providerDefault: T
    val providers: ConcurrentHashMap<String, T>
    fun addProvider(provider: T)
    fun onSuccess(appTask: AppTask)
    fun onFail(appTask: AppTask)
}