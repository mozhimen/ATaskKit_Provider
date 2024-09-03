package com.mozhimen.taskk.provider.tradition.utils

import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.kotlin.deleteFile
import com.mozhimen.kotlin.utilk.kotlin.deleteFolder
import com.mozhimen.kotlin.utilk.kotlin.getStrFilePathNoExtension
import com.mozhimen.kotlin.utilk.kotlin.getStrFolderPath
import com.mozhimen.kotlin.utilk.kotlin.isFileExist
import com.mozhimen.kotlin.utilk.kotlin.isFolderExist
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.utils.TaskProviderUtil.TAG

/**
 * @ClassName NetKAppUtil
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/22
 * @Version 1.0
 */
object NetKAppUtil {
    /**
     * 删除Apk文件
     */
    @JvmStatic
    fun deleteFileApk(appTask: AppTask): Boolean {
        try {
            //删除文件
            if (appTask.filePathNameExt.isFileExist()) {
                appTask.filePathNameExt.deleteFile()
                UtilKLogWrapper.d(TAG, "deleteFileApk: deleteFile")
            }

            //删除目录
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
}