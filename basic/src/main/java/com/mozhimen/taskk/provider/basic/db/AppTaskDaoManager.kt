package com.mozhimen.taskk.provider.basic.db

import com.mozhimen.kotlin.utilk.commons.IUtilK
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName AppTaskDaoManager
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 16:08
 * @Version 1.0
 */
class AppTaskDaoManager : IUtilK {
    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    ////////////////////////////////////////////////////////////////////////////

    private val _daoMap = ConcurrentHashMap<String, AppTaskDaoProvider>()

    ////////////////////////////////////////////////////////////////////////////

    fun default(): AppTaskDaoProvider {
        return with("")
    }

    fun with(channel: String): AppTaskDaoProvider {
        var sp = _daoMap[channel]
        if (sp == null) {
            sp = AppTaskDaoProvider(channel)
            _daoMap[channel] = sp
        }
        return sp
    }

    ////////////////////////////////////////////////////////////////////////////

    private object INSTANCE {
        val holder = AppTaskDaoManager()
    }
}