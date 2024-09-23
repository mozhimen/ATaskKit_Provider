package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.cons.CState
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.download.okdownload.TaskDownloadOkDownload

/**
 * @ClassName TaskProviderDownloadApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
@OPermission_INTERNET
class TaskDownloadOkDownloadApk(iTaskLifecycle: ITaskLifecycle) : TaskDownloadOkDownload(iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    @OptIn(OApiInit_InApplication::class)
    override fun taskStart(appTask: AppTask) {
        if (InstallKManager.hasPackageName_lessThanInstalledVersionCode(appTask.apkPackageName, appTask.apkVersionCode)) {
            UtilKLogWrapper.d(TAG, "taskStart: hasPackageNameAndSatisfyVersion")
            onTaskFinished(CState.STATE_TASK_SUCCESS, STaskFinishType.SUCCESS, appTask)//onInstallSuccess(appTask)
            return
        }
        super.taskStart(appTask)
    }
}