package com.mozhimen.taskk.task.provider.utils

import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.basick.utilk.kotlin.deleteFile
import com.mozhimen.basick.utilk.kotlin.deleteFolder
import com.mozhimen.basick.utilk.kotlin.getStrFilePathNoExtension
import com.mozhimen.basick.utilk.kotlin.getStrFolderPath
import com.mozhimen.basick.utilk.kotlin.isFileExist
import com.mozhimen.basick.utilk.kotlin.isFolderExist
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
}