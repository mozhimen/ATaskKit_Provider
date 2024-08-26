package com.mozhimen.taskk.provider.apk.utils

import android.util.Log
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.taskk.provider.apk.TaskProviderApk
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager

/**
 * @ClassName AppTaskUtil
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/22
 * @Version 1.0
 */
object AppTaskUtil : IUtilK {
    @OptIn(OApiInit_InApplication::class, OPermission_INTERNET::class)
    fun generateAppTask_ofDb_installed_version(taskManager: ATaskManager, appTask: AppTask): AppTask {
        val installedPackageBundle = InstallKManager.getPackageBundle_ofPackageName(appTask.apkPackageName)//小于已安装版本
        val appTask_ofDb = AppTaskDaoManager.get_ofTaskId_ApkPackageName_ApkVersionCode(appTask.taskId, appTask.apkPackageName, appTask.apkVersionCode)
        if (installedPackageBundle == null) {//未安装
            if (appTask_ofDb == null) {
                UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask_ofDb == null")
                when {
                    appTask.isTaskCreate() -> taskManager.onTaskCreate(appTask, false)
                    appTask.isTaskUpdate() -> taskManager.onTaskCreate(appTask, true)
                    appTask.isTaskUnAvailable() -> taskManager.onTaskUnavailable(appTask)
                    appTask.isTaskSuccess() -> taskManager.onTaskFinish(appTask, STaskFinishType.SUCCESS)
                }
                return appTask
            } else {
                if (appTask.isTaskSuccess()) {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask_ofDb != null appTask.isTaskSuccess()")
                    when {
                        appTask.isTaskCreate() -> taskManager.onTaskCreate(appTask, false)
                        appTask.isTaskUpdate() -> taskManager.onTaskCreate(appTask, true)
                    }
                } else {
//                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask_ofDb != null")
                }
                return appTask
            }
        } else {//已安装
            if (appTask.apkVersionCode > installedPackageBundle.versionCode) {
                if (appTask_ofDb != null && !appTask.isTaskUpdate()) {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode > installedPackageBundle.versionCode appTask_ofDb != null && !appTask.isTaskCreate()")
                    taskManager.onTaskCreate(appTask, true)
                } else if (appTask_ofDb == null) {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode > installedPackageBundle.versionCode appTask_ofDb == null")
                    taskManager.onTaskCreate(appTask, true)
                } else {
//                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode > installedPackageBundle.versionCode else")
                }
                return appTask
            } else {
                if (appTask_ofDb != null && !appTask.isTaskSuccess()) {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode <= installedPackageBundle.versionCode appTask_ofDb != null && !appTask.isTaskSuccess()")
                    taskManager.onTaskFinish(appTask, STaskFinishType.SUCCESS)
                } else if (appTask_ofDb == null) {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode <= installedPackageBundle.versionCode appTask_ofDb == null")
                    taskManager.onTaskFinish(appTask, STaskFinishType.SUCCESS)
                } else {
//                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode <= installedPackageBundle.versionCode else")
                }
                return appTask
            }
        }
    }
}