package com.mozhimen.netk.app.install

import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.basick.utilk.kotlin.collections.ifNotEmptyOr
import com.mozhimen.basick.utilk.kotlin.deleteFile
import com.mozhimen.basick.utilk.kotlin.deleteFolder
import com.mozhimen.basick.utilk.kotlin.getStrFilePathNoExtension
import com.mozhimen.basick.utilk.kotlin.getStrFolderPath
import com.mozhimen.basick.utilk.kotlin.isFileExist
import com.mozhimen.basick.utilk.kotlin.isFolderExist
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.netk.app.NetKApp
import com.mozhimen.netk.app.cons.CNetKAppState
import com.mozhimen.netk.app.task.db.AppTask
import com.mozhimen.netk.app.task.db.AppTaskDaoManager

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
        val list = AppTaskDaoManager.getAppTasksByApkPackageName(apkPackageName)
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
        NetKApp.onUninstallSuccess(appTask)
    }

    ///////////////////////////////////////////////////////////////////

    /**
     * 删除Apk文件
     */
    @JvmStatic
    fun deleteFileApk(appTask: AppTask): Boolean {
        try {
            if (appTask.apkPathName.isFileExist()) {
                appTask.apkPathName.deleteFile()
                UtilKLogWrapper.d(TAG, "deleteFileApk: deleteFile")
            }

            val gameFolder = appTask.apkPathName.getStrFilePathNoExtension()?.getStrFolderPath()
            if (gameFolder != null && gameFolder.isFolderExist()/*appTask.apkFileName.endsWith(".npk") && */) {//如果是npk,删除解压的文件夹
                gameFolder.deleteFolder()
                UtilKLogWrapper.d(TAG, "deleteFileApk: deleteFolder")
            }

            UtilKLogWrapper.w(TAG, "deleteFileApk path ${appTask.apkPathName} name ${appTask.apkFileName} gameFolder $gameFolder")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}