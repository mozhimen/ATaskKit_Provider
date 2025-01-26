package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle

/**
 * @ClassName TaskProviderUninstallDefault
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
class TaskUninstallApk(taskManager: ATaskManagerProvider, iTaskLifecycle: ITaskLifecycle?) : ATaskUninstall(taskManager,iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }
}