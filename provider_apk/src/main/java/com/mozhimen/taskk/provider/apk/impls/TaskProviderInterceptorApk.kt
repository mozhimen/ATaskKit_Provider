package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.java.io.deleteFolder
import com.mozhimen.basick.utilk.kotlin.deleteFile
import com.mozhimen.basick.utilk.kotlin.isFileExist
import com.mozhimen.basick.utilk.kotlin.strFilePath2file
import com.mozhimen.cachek.datastore.CacheKDS
import com.mozhimen.cachek.datastore.temps.CacheKDSVarPropertyBoolean
import com.mozhimen.taskk.provider.apk.interfaces.ITaskProviderInterceptorApk
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName TaskProviderInterceptorApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/22
 * @Version 1.0
 */
object TaskProviderInterceptorApk : ITaskProviderInterceptorApk {
    private val _cacheKDSProvider by lazy { CacheKDS.instance.with(NAME) }
    var isDeleteApkFile by CacheKDSVarPropertyBoolean(_cacheKDSProvider, "is_delete_apk_file", true)
    var isAutoInstall by CacheKDSVarPropertyBoolean(_cacheKDSProvider, "is_auto_install", true)

    override fun isAutoDeleteOrgFiles(): Boolean {
        return isDeleteApkFile
    }

    override fun deleteOrgFiles(appTask: AppTask) {
        try {
            //删除文件
            if (appTask.filePathNameExt.isNotEmpty() && appTask.filePathNameExt.isFileExist()) {
                appTask.filePathNameExt.deleteFile()
                UtilKLogWrapper.d(TAG, "deleteFileApk: deleteFile")
            }

            if (appTask.taskUnzipEnable && appTask.taskUnzipFilePath.isNotEmpty() && appTask.taskUnzipFilePath.isFileExist()) {
                val filePathUnzip = appTask.taskUnzipFilePath.strFilePath2file()
                val filePathUnzipParent = filePathUnzip.parentFile
                filePathUnzipParent?.deleteFolder()
            }

            UtilKLogWrapper.w(TAG, "deleteFileApk filePathNameExt ${appTask.filePathNameExt} taskUnzipFilePath ${appTask.fileNameExt}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun isAutoInstall(): Boolean {
        return isAutoInstall
    }
}