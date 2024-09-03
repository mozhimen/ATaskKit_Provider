package com.mozhimen.taskk.provider.apk.impls

import android.annotation.SuppressLint
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall
import com.mozhimen.taskk.provider.basic.cons.CState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName TaskProviderUninstallDefault
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
class TaskUninstallApk(iTaskLifecycle: ITaskLifecycle?) : ATaskUninstall(iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }
}