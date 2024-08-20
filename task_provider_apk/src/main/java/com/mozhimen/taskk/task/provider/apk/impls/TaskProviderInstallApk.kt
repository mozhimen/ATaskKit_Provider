package com.mozhimen.taskk.task.provider.apk.impls

import com.mozhimen.basick.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.basick.utilk.wrapper.UtilKAppInstall
import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderInstall
import com.mozhimen.taskk.task.provider.db.AppTask

/**
 * @ClassName NetKAppInstallProviderDefault
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
@OPermission_REQUEST_INSTALL_PACKAGES
class TaskProviderInstallApk : ITaskProviderInstall {
    override fun getSupportFileExtensions(): List<String> {
        return listOf("apk")
    }

    override fun taskStart(appTask: AppTask) {
        if (appTask.taskUnzipEnable) {
            UtilKAppInstall.install_ofView(appTask.taskUnzipFilePath)
        } else {
            UtilKAppInstall.install_ofView(appTask.filePathNameExt)
        }
    }

    override fun taskPause(appTask: AppTask) {

    }

    override fun taskResume(appTask: AppTask) {

    }

    override fun taskCancel(appTask: AppTask) {

    }
}