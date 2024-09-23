package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.java.io.deleteFolder
import com.mozhimen.kotlin.utilk.kotlin.deleteFile
import com.mozhimen.kotlin.utilk.kotlin.isFileExist
import com.mozhimen.kotlin.utilk.kotlin.strFilePath2file
import com.mozhimen.cachek.datastore.CacheKDS
import com.mozhimen.cachek.datastore.temps.CacheKDSVarPropertyBoolean
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskInterceptor

/**
 * @ClassName TaskProviderInterceptorApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/22
 * @Version 1.0
 */
object TaskInterceptorApk : ITaskInterceptor {
    private val _cacheKDSProvider by lazy { CacheKDS.instance.with(NAME) }
    var is_delete_apk_file by CacheKDSVarPropertyBoolean(_cacheKDSProvider, "is_delete_apk_file", true)

    override fun isAutoDeleteOrgFiles(): Boolean {
        return is_delete_apk_file
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
}