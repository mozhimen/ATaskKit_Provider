package com.mozhimen.taskk.provider.core.utils

import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.taskk.provider.basic.cons.CState
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.core.TaskProvider

/**
 * @ClassName AppTaskUtil
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/22
 * @Version 1.0
 */
object AppTaskUtil {
    @OptIn(OApiInit_InApplication::class)
    fun generateAppTask_ofDb_installed_version(appTask: AppTask): AppTask {
        val installedPackageBundle = InstallKManager.getPackageBundle_ofPackageName(appTask.apkPackageName)//小于已安装版本
        val appTask_ofDb = AppTaskDaoManager.get_ofTaskId_ApkPackageName_ApkVersionCode(appTask.taskId, appTask.apkPackageName, appTask.apkVersionCode)
        if (installedPackageBundle == null) {//未安装
            if (appTask_ofDb == null) {
                appTask.toNewTaskState(appTask.taskState)
                TaskProvider.instance.iTaskProviderLifecycle.
                when (appTask.taskState) {
                    CState.STATE_TASK_SUCCESS ->  TaskProvider.instance.onTaskSuccess(appTask)
                    CState.STATE_TASK_UNAVAILABLE -> {
                        onTaskUnavailable(appTask)
                    }

                    CState.STATE_TASK_UPDATE -> {
                        onTaskCreate(appTask, true)
                    }
                }
            }
        } else {//已安装
            if (appTask.apkVersionCode > installedPackageBundle.versionCode) {
                TaskProvider.instance.onTaskCreate(appTask, true)
            } else {
                TaskProvider.instance.onTaskSuccess(appTask)
            }
        }
        return appTask
    }
}