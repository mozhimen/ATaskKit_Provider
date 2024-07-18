package com.mozhimen.netk.app.verify

import android.text.TextUtils
import android.util.Log
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.basick.utilk.java.io.UtilKFileDir
import com.mozhimen.basick.utilk.java.io.file2strFilePath
import com.mozhimen.basick.utilk.java.io.file2strMd5Hex_use_ofStream
import com.mozhimen.netk.app.NetKApp
import com.mozhimen.netk.app.cons.CNetKAppErrorCode
import com.mozhimen.netk.app.cons.CNetKAppState
import com.mozhimen.netk.app.download.mos.intAppErrorCode2appDownloadException
import com.mozhimen.netk.app.task.db.AppTask
import com.mozhimen.netk.app.unzip.NetKAppUnzipManager
import java.io.File

/**
 * @ClassName AppVerifyManager
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/8 17:01
 * @Version 1.0
 */
@OApiInit_InApplication
internal object NetKAppVerifyManager : IUtilK {

    @JvmStatic
    fun verify(appTask: AppTask) {
        if (appTask.isTaskUnzip()) {
            UtilKLogWrapper.d(TAG, "verify: the task already verify")
            return
        }
        /**
         * [CNetKAppState.STATE_VERIFYING]
         */
        NetKApp.onVerifying(appTask)

        UtilKLogWrapper.d(TAG, "verify: apkFileName ${appTask.apkFileName}")

        if (appTask.apkFileName.endsWith(".apk") || appTask.apkFileName.endsWith(".npk")) {//如果文件以.npk结尾则先解压
            verifyApp(appTask)
        } else {
            /**
             * [CNetKAppState.STATE_VERIFY_FAIL]
             */
            NetKApp.onVerifyFail(appTask, CNetKAppErrorCode.CODE_VERIFY_FORMAT_INVALID.intAppErrorCode2appDownloadException())
            UtilKLogWrapper.d(TAG, "verifyAndUnzipNpk: getFilesDownloadsDir is null")
        }
    }

    /**
     * 安装.npk文件
     */
    private fun verifyApp(appTask: AppTask) {
        if (!isNeedVerify(appTask)) {//如果文件没有MD5值或者为空，则不校验 直接去安装
            onVerifySuccess(appTask, File(NetKApp.getDownloadPath() ?: return, appTask.apkFileName))
            return
        }

        if (NetKAppUnzipManager.isUnziping(appTask)) {
            UtilKLogWrapper.d(TAG, "verifyAndUnzipNpk: isUnziping")
            return//正在解压中，不进行操作
        }

        val externalFilesDir = NetKApp.getDownloadPath() ?: run {
            /**
             * [CNetKAppState.STATE_VERIFY_FAIL]
             */
            NetKApp.onVerifyFail(appTask, CNetKAppErrorCode.CODE_VERIFY_DIR_NULL.intAppErrorCode2appDownloadException())
            UtilKLogWrapper.e(TAG, "verifyAndUnzipNpk: getFilesDownloadsDir is null")
            return
        }
        val fileApk = File(externalFilesDir, appTask.apkFileName)
        if (!fileApk.exists()) {
            /**
             * [CNetKAppState.STATE_VERIFY_FAIL]
             */
            NetKApp.onVerifyFail(appTask, CNetKAppErrorCode.CODE_VERIFY_FILE_NOT_EXIST.intAppErrorCode2appDownloadException())
            UtilKLogWrapper.e(TAG, "verifyAndUnzipNpk: download file fail")
            return
        }

        if (isNeedVerify(appTask)) {
            val apkFileMd5Locale = fileApk.file2strMd5Hex_use_ofStream()//取文件的MD5值
            if (!TextUtils.equals(appTask.apkFileMd5, apkFileMd5Locale)) {
                /**
                 * [CNetKAppState.STATE_VERIFY_FAIL]
                 */
                NetKApp.onVerifyFail(appTask, CNetKAppErrorCode.CODE_VERIFY_MD5_FAIL.intAppErrorCode2appDownloadException())
                UtilKLogWrapper.e(TAG, "verifyAndUnzipNpk: download file fail")

                NetKApp.taskRetry(appTask.apply {
                    apkPathName = fileApk.file2strFilePath()
                })
                return
            }
        }

        onVerifySuccess(appTask, fileApk)
    }

    private fun onVerifySuccess(appTask: AppTask, fileApk: File) {
        /**
         * [CNetKAppState.STATE_VERIFY_SUCCESS]
         */
        NetKApp.onVerifySuccess(appTask)//检测通过，去安装

        NetKAppUnzipManager.unzip(appTask.apply {
            apkPathName = fileApk.file2strFilePath()
        })
    }

    //////////////////////////////////////////////////////////////////

    /**
     * 判断是否需要校验MD5值
     * 1、NPK不需要校验MD5值
     * 2、如果是使用站内地址下载，不用校验MD5值
     * 3、如果使用站外地址，且没有站内地址，且第一次校验失败，则第二次时不用校验
     */
    @JvmStatic
    private fun isNeedVerify(appTask: AppTask): Boolean {
//        if (appTask.apkName.endsWith(".npk"))
//            return false
//        if (appTask.downloadUrlCurrent == appTask.downloadUrl) {//如果是使用站内地址下载，不用校验MD5值
//            return false
//        }
        return appTask.apkVerifyNeed && appTask.apkFileMd5.isNotEmpty()
    }
}