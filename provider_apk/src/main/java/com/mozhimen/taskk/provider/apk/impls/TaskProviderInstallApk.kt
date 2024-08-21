package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.basick.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.basick.utilk.wrapper.UtilKAppInstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderInstall
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderLifecycle

/**
 * @ClassName NetKAppInstallProviderDefault
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
class TaskProviderInstallApk(iTaskProviderLifecycle: ITaskProviderLifecycle) : ATaskProviderInstall(iTaskProviderLifecycle) {
    override fun getSupportFileExtensions(): List<String> {
        return listOf("apk")
    }

    @OPermission_REQUEST_INSTALL_PACKAGES
    override fun taskStart(appTask: AppTask) {
        if (appTask.taskUnzipEnable) {
            UtilKAppInstall.install_ofView(appTask.taskUnzipFilePath)
        } else {
            UtilKAppInstall.install_ofView(appTask.filePathNameExt)
        }
        onTaskStarted(CTaskState.STATE_INSTALLING, appTask)
    }

    override fun taskResume(appTask: AppTask) {
        onTaskStarted(CTaskState.STATE_INSTALLING, appTask)
    }

    override fun taskPause(appTask: AppTask) {
        onTaskPaused(CTaskState.STATE_INSTALL_PAUSE, appTask)
    }

    override fun taskCancel(appTask: AppTask) {
        onTaskFinished(CTaskState.STATE_INSTALL_CANCEL, STaskFinishType.CANCEL, appTask)
    }
}