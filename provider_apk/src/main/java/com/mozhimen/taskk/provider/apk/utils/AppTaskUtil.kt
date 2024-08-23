package com.mozhimen.taskk.provider.apk.utils

import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.taskk.provider.apk.TaskProvidersApk
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.core.bases.ATaskProviders

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
                when {
                    appTask.isTaskCreate() -> TaskProvidersApk.instance.onTaskCreate(appTask, false)
                    appTask.isTaskUpdate() -> TaskProvidersApk.instance.onTaskCreate(appTask, true)
                    appTask.isTaskUnAvailable() -> TaskProvidersApk.instance.onTaskUnavailable(appTask)
                    appTask.isTaskSuccess() -> TaskProvidersApk.instance.onTaskSuccess(appTask)
                }
            }
        } else {//已安装
            if (appTask.apkVersionCode > installedPackageBundle.versionCode) {
                TaskProvidersApk.instance.onTaskCreate(appTask, true)
            } else {
                TaskProvidersApk.instance.onTaskSuccess(appTask)
            }
        }
        return appTask
    }
}