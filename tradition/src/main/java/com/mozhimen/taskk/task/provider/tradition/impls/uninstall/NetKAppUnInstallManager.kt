package com.mozhimen.taskk.task.provider.tradition.impls.uninstall

import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.basick.utilk.kotlin.collections.ifNotEmptyOr
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.netk.app.NetKApp
import com.mozhimen.taskk.task.provider.db.AppTask
import com.mozhimen.taskk.task.provider.db.AppTaskDaoManager

/**
 * @ClassName NetKAppUnInstallManager
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/12/6 14:51
 * @Version 1.0
 */
internal object NetKAppUnInstallManager : IUtilK {
    @OptIn(OApiInit_InApplication::class)
    @JvmStatic
    fun onUninstallSuccess(apkPackageName: String) {
        val list = AppTaskDaoManager.gets_ofApkPackageName(apkPackageName)
        list.ifNotEmptyOr({
            it.forEach { appTask ->
                UtilKLogWrapper.d(TAG, "onUninstallSuccess: apkPackageName $apkPackageName")
                onUninstallSuccess(appTask)
            }
        }, {
            UtilKLogWrapper.d(TAG, "onUninstallSuccess: removePackage $apkPackageName")
            InstallKManager.removePackage(apkPackageName)
        })
    }

    @OptIn(OApiInit_InApplication::class)
    @JvmStatic
    fun onUninstallSuccess(appTask: AppTask) {
        InstallKManager.removePackage(appTask.apkPackageName)

        /**
         * [CNetKAppState.STATE_UNINSTALL_SUCCESS]
         */
        NetKApp.instance.onUninstallSuccess(appTask)
    }
}