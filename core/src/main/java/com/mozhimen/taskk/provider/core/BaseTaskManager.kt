package com.mozhimen.taskk.provider.core

import android.content.Context
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskManager
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/1/22
 * @Version 1.0
 */
@OApiInit_InApplication
abstract class BaseTaskManager {

    private val _tmpMap = ConcurrentHashMap<String, BaseTaskManagerProvider>()

    /////////////////////////////////////////////////////////////////////////////////////

    abstract fun getTaskManagerProviders(): Map<String, BaseTaskManagerProvider>

    /////////////////////////////////////////////////////////////////////////////////////

    fun with(name: String): BaseTaskManagerProvider? {
        var sp = _tmpMap[name]
        if (sp == null) {
            sp = getTaskManagerProviders().get(name) ?: return null
            _tmpMap[name] = sp
        }
        return sp
    }

    fun init(context: Context) {
        getTaskManagerProviders().forEach { it.value.init(context) }
    }
}