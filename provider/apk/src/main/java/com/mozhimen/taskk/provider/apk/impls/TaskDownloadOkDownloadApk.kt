package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.task.download.okdownload.TaskDownloadOkDownload

/**
 * @ClassName TaskProviderDownloadApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
open class TaskDownloadOkDownloadApk(
    taskManager: ATaskManagerProvider, iTaskLifecycle: ITaskLifecycle,
) : TaskDownloadOkDownload(taskManager, iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    @OptIn(OApiInit_InApplication::class)
    override fun taskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        if (InstallKManager.hasPackageName_lessThanInstalledVersionCode(appTask.apkPackageName, appTask.apkVersionCode)) {
            UtilKLogWrapper.d(TAG, "taskStart: hasPackageNameAndSatisfyVersion")
            onTaskFinished(ATaskState.STATE_DOWNLOAD_SUCCESS, appTask, taskNodeQueueName, STaskFinishType.SUCCESS)//onInstallSuccess(appTask)
            return
        }
        super.taskStart(appTask, taskNodeQueueName)
    }
}