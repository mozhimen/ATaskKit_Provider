package com.mozhimen.taskk.provider.test

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.taskk.provider.core.BaseTaskManager
import com.mozhimen.taskk.provider.core.BaseTaskManagerProvider

/**
 * @ClassName MainTaskManager
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/1/23
 * @Version 1.0
 */
@OptIn(OApiInit_InApplication::class)
object MainTaskManager : BaseTaskManager() {

    fun default(): MainTaskManagerProvider =
        with("") as MainTaskManagerProvider

    override fun getTaskManagerProviders(): Map<String, BaseTaskManagerProvider> {
        return mapOf("" to MainTaskManagerProvider())
    }
}