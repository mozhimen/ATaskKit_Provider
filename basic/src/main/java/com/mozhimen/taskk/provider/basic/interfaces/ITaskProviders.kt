package com.mozhimen.taskk.provider.basic.interfaces

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskProvider

/**
 * @ClassName ITaskProviderSets
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
interface ITaskProviders {
    fun getChains(): Map<String, List<@ATaskName String>>
    fun getProviders(): List<ATaskProvider>
}