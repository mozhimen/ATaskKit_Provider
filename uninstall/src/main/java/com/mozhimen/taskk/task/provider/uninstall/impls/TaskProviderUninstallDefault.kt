package com.mozhimen.taskk.task.provider.uninstall.impls

import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderUninstall
import com.mozhimen.taskk.task.provider.db.AppTask

/**
 * @ClassName TaskProviderUninstallDefault
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
class TaskProviderUninstallDefault : ITaskProviderUninstall {
    override fun getSupportFileExtensions(): List<String> {
        return listOf("apk")
    }

    override fun process(appTask: AppTask) {

    }
}