package com.mozhimen.taskk.task.provider.apk.impls

import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.taskk.task.provider.commons.ITaskProviderLifecycle
import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderUninstall
import com.mozhimen.taskk.task.provider.db.AppTask
import com.mozhimen.taskk.task.provider.db.AppTaskDaoManager

/**
 * @ClassName TaskProviderUninstallDefault
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
class TaskProviderUninstallApk(private val _iTaskProviderLifecycle: ITaskProviderLifecycle) : ITaskProviderUninstall {

    override fun getSupportFileExtensions(): List<String> {
        return listOf("apk")
    }

    ////////////////////////////////////////////////////

    fun onSucceeded(taskState: Int, apkPackageName: String) {
        val appTasks = AppTaskDaoManager.gets_ofApkPackageName(apkPackageName)
        if (appTasks.isNotEmpty()) {
            UtilKLogWrapper.d(TAG, "onUninstallSuccess: apkPackageName $apkPackageName")
            appTasks.forEach { appTask ->
                onTaskSucceeded(taskState, appTask)
            }
        } else {
            UtilKLogWrapper.d(TAG, "onUninstallSuccess: removePackage $apkPackageName")
            removePackage(apkPackageName)
        }
    }

    override fun onTaskSucceeded(taskState: Int, appTask: AppTask) {
        super.onTaskSucceeded(taskState, appTask)
        removePackage(appTask.apkPackageName)
        //
        _iTaskProviderLifecycle.onTaskSucceeded(taskState, appTask)
    }

    override fun onTaskFailed(taskState: Int, appTask: AppTask) {
        super.onTaskFailed(taskState, appTask)
        _iTaskProviderLifecycle.onTaskFailed(taskState, appTask)
    }

    ////////////////////////////////////////////////////

    @OptIn(OApiInit_InApplication::class)
    private fun removePackage(apkPackageName: String) {
        InstallKManager.removePackage(apkPackageName)
    }
}