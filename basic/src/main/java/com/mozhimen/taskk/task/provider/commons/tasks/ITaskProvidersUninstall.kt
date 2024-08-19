package com.mozhimen.taskk.task.provider.commons.tasks

import com.mozhimen.taskk.task.provider.db.AppTask

/**
 * @ClassName ITaskUninstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
interface ITaskProvidersUninstall {
    fun onUninstallSuccess(appTask: AppTask) {}//应用卸载的监听
}