package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderLifecycle
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.download.okdownload.TaskProviderDownloadOkDownload

/**
 * @ClassName TaskProviderDownloadApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
class TaskProviderDownloadOkDownloadApk(iTaskProviderLifecycle: ITaskProviderLifecycle) : TaskProviderDownloadOkDownload(iTaskProviderLifecycle) {
    @OptIn(OApiInit_InApplication::class)
    override fun taskStart(appTask: AppTask) {
        if (InstallKManager.hasPackageName_satisfyVersionCode(appTask.apkPackageName, appTask.apkVersionCode)) {
            UtilKLogWrapper.d(TAG, "taskStart: hasPackageNameAndSatisfyVersion")
            /**
             * [CTaskState.STATE_INSTALL_SUCCESS]
             */
            onTaskFinished(CTaskState.STATE_INSTALL_SUCCESS, STaskFinishType.SUCCESS, appTask)//onInstallSuccess(appTask)
            return
        }
        super.taskStart(appTask)
    }
}