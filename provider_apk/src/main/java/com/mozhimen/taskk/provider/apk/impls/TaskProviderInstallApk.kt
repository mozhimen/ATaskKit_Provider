package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.basick.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.basick.utilk.wrapper.UtilKAppInstall
import com.mozhimen.taskk.provider.apk.cons.CExt
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
        return listOf(CExt.EXT_APK)
    }

    @OPermission_REQUEST_INSTALL_PACKAGES
    override fun taskStart(appTask: AppTask) {
        if (appTask.taskUnzipEnable) {
            UtilKAppInstall.install_ofView(appTask.taskUnzipFilePath)
        } else {
            UtilKAppInstall.install_ofView(appTask.filePathNameExt)
        }
        super.taskStart(appTask)
    }
}