package com.mozhimen.taskk.task.provider.commons

import com.mozhimen.taskk.task.provider.annors.ATaskName

/**
 * @ClassName ITaskProviderSets
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskProviderSets {
    fun getChains(): Map<String, List<@ATaskName String>>
    fun getProviders(): List<ITaskProvider>
}