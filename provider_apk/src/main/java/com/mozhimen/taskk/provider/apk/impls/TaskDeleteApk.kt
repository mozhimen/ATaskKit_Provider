package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDelete
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle

/**
 * @ClassName TaskDeleteApk
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/9/22 23:45
 * @Version 1.0
 */
class TaskDeleteApk(iTaskLifecycle: ITaskLifecycle?) : ATaskDelete(iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }
}