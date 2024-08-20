package com.mozhimen.taskk.task.provider.utils

import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.basick.utilk.kotlin.deleteFile
import com.mozhimen.basick.utilk.kotlin.deleteFolder
import com.mozhimen.basick.utilk.kotlin.getStrFilePathNoExtension
import com.mozhimen.basick.utilk.kotlin.getStrFolderPath
import com.mozhimen.basick.utilk.kotlin.isFileExist
import com.mozhimen.basick.utilk.kotlin.isFolderExist
import com.mozhimen.taskk.task.provider.cons.CState
import com.mozhimen.taskk.task.provider.cons.CTaskState
import com.mozhimen.taskk.task.provider.db.AppTask

/**
 * @ClassName TaskProviderUtil
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
object TaskProviderUtil : IUtilK {
    /**
     * 删除Apk文件
     */
    @JvmStatic
    fun deleteFileApk(appTask: AppTask): Boolean {
        try {
            if (appTask.filePathNameExt.isFileExist()) {
                appTask.filePathNameExt.deleteFile()
                UtilKLogWrapper.d(TAG, "deleteFileApk: deleteFile")
            }

            val gameFolder = appTask.filePathNameExt.getStrFilePathNoExtension()?.getStrFolderPath()
            if (gameFolder != null && gameFolder.isFolderExist()/*appTask.apkFileName.endsWith(".npk") && */) {//如果是npk,删除解压的文件夹
                gameFolder.deleteFolder()
                UtilKLogWrapper.d(TAG, "deleteFileApk: deleteFolder")
            }

            UtilKLogWrapper.w(TAG, "deleteFileApk path ${appTask.filePathNameExt} name ${appTask.fileNameExt} gameFolder $gameFolder")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    @JvmStatic
    fun intTaskState2strTaskState(state: Int): String =
        when (state) {
//            CNetKAppState.STATE_DOWNLOAD_WAIT -> "下载等待"
            CTaskState.STATE_DOWNLOADING -> "下载中 "
            CTaskState.STATE_DOWNLOAD_PAUSE -> "下载暂停"
            CTaskState.STATE_DOWNLOAD_CANCEL -> "下载取消"
            CTaskState.STATE_DOWNLOAD_SUCCESS -> "下载成功"
            CTaskState.STATE_DOWNLOAD_FAIL -> "下载失败"

            CTaskState.STATE_VERIFYING -> "验证中 "
            CTaskState.STATE_VERIFY_CANCEL -> "验证取消"
            CTaskState.STATE_VERIFY_SUCCESS -> "验证成功"
            CTaskState.STATE_VERIFY_FAIL -> "验证失败"

            CTaskState.STATE_UNZIPING -> "解压中 "
            CTaskState.STATE_UNZIP_CANCEL -> "解压取消"
            CTaskState.STATE_UNZIP_SUCCESS -> "解压成功"
            CTaskState.STATE_UNZIP_FAIL -> "解压失败"

            CTaskState.STATE_INSTALLING -> "安装中 "
            CTaskState.STATE_INSTALL_CANCEL -> "安装取消"
            CTaskState.STATE_INSTALL_SUCCESS -> "安装成功"
            CTaskState.STATE_INSTALL_FAIL -> "安装失败"

            CTaskState.STATE_UNINSTALLING -> "卸载中 "
            CTaskState.STATE_UNINSTALL_CANCEL -> "卸载取消"
            CTaskState.STATE_UNINSTALL_SUCCESS -> "卸载成功"
            CTaskState.STATE_UNINSTALL_FAIL -> "卸载失败"

            CState.STATE_TASK_CREATE -> "任务创建"
            CState.STATE_TASK_UPDATE -> "任务更新"
//            CState.STATE_TASK_WAIT -> "任务等待"
            CState.STATE_TASK_PAUSE -> "任务暂停"
            CState.STATE_TASK_CANCEL -> "任务取消"
            CState.STATE_TASK_SUCCESS -> "任务成功"
            CState.STATE_TASK_FAIL -> "任务失败"
            else -> "任务中 "
        }

}