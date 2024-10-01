package com.mozhimen.taskk.provider.apk.utils

import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.taskk.provider.basic.annors.AState
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
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
    @OptIn(OApiInit_InApplication::class)
    fun generateAppTask_ofDb_installed_version(taskManager: ATaskManager, appTask: AppTask, @ATaskQueueName taskQueueName: String): AppTask {
        val installedPackageBundle = InstallKManager.getPackageBundle_ofPackageName(appTask.apkPackageName)//小于已安装版本
        val appTask_ofDb = AppTaskDaoManager.get_ofTaskId_ApkPackageName_ApkVersionCode(appTask.id, appTask.apkPackageName, appTask.apkVersionCode)
        if (installedPackageBundle == null) {//未安装
            if (appTask_ofDb == null) {
                UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask_ofDb == null")
                when {
                    appTask.isTaskCreate() -> taskManager.onTaskCreate(appTask, taskQueueName, false)
                    appTask.isTaskUpdate() -> taskManager.onTaskCreate(appTask, taskQueueName, true)
                    appTask.isTaskUnAvailable() -> taskManager.onTaskUnavailable(appTask, taskQueueName)
                    appTask.isTaskSuccess(taskManager, taskQueueName) -> taskManager.onTaskFinish(appTask, taskQueueName, STaskFinishType.SUCCESS)
                }
                return appTask
            } else {//存在库中
                if (appTask_ofDb.isTaskSuccess(taskManager, taskQueueName)) {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask_ofDb != null appTask_ofDb.isTaskSuccess()")
                    taskManager.onTaskCreate(appTask, taskQueueName, false)
                } else if (appTask_ofDb.isTaskProcess(taskManager, taskQueueName)) {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask_ofDb != null appTask_ofDb.isTaskProcess")
                    return appTask_ofDb
                } else {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask_ofDb != null")
                }
                return appTask
            }
        } else {//已安装
            if (appTask.apkVersionCode > installedPackageBundle.versionCode) {
                if (appTask_ofDb != null && !appTask.isTaskUpdate() && !appTask.isTaskProcess(taskManager, taskQueueName)) {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode > installedPackageBundle.versionCode appTask_ofDb != null && !appTask.isTaskCreate()")
                    taskManager.onTaskCreate(appTask, taskQueueName, true)
                } else if (appTask_ofDb == null) {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode > installedPackageBundle.versionCode appTask_ofDb == null")
                    taskManager.onTaskCreate(appTask, taskQueueName, true)
                } else {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode > installedPackageBundle.versionCode else")
                }
                return appTask
            } else {
                if (appTask_ofDb != null && !appTask.isTaskSuccess(taskManager, taskQueueName)) {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode <= installedPackageBundle.versionCode appTask_ofDb != null && !appTask.isTaskSuccess()")
                    taskManager.onTaskFinish(appTask, taskQueueName, STaskFinishType.SUCCESS)
                } else if (appTask_ofDb == null) {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode <= installedPackageBundle.versionCode appTask_ofDb == null")
                    taskManager.onTaskFinish(appTask, taskQueueName, STaskFinishType.SUCCESS)
                } else {
                    UtilKLogWrapper.d(TAG, "generateAppTask_ofDb_installed_version: appTask.apkVersionCode <= installedPackageBundle.versionCode else")
                }
                return appTask
            }
        }
    }
}