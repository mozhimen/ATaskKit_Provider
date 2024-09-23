package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.java.io.file2strMd5Hex_use_ofStream
import com.mozhimen.kotlin.utilk.kotlin.isFileNotExist
import com.mozhimen.kotlin.utilk.kotlin.strFilePath2file
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskVerify
import com.mozhimen.taskk.provider.basic.cons.CErrorCode
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.intErrorCode2taskException
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle

/**
 * @ClassName TaskProviderVerifyApk
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/8/21 21:56
 * @Version 1.0
 */
class TaskVerifyApk(iTaskLifecycle: ITaskLifecycle) : ATaskVerify(iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    override fun taskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        if (appTask.atTaskUnzip()) {
            UtilKLogWrapper.d(TAG, "verify: the task already verify")
            return
        }
        UtilKLogWrapper.d(TAG, "verify: filePathNameExt ${appTask.filePathNameExt} fileNameExt ${appTask.fileNameExt}")

        super.taskStart(appTask, taskQueueName)

        if (appTask.taskVerifyEnable) {//如果文件没有MD5值或者为空，则不校验 直接去安装
            startVerify(appTask, taskQueueName)
        } else {
            onTaskFinished(ATaskState.STATE_VERIFY_SUCCESS, appTask, taskQueueName, STaskFinishType.SUCCESS)
        }
    }

    private fun startVerify(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        if (appTask.filePathNameExt.isEmpty()) {
            UtilKLogWrapper.e(TAG, "startVerify: getFilesDownloadsDir is null")
            onTaskFinished(ATaskState.STATE_VERIFY_FAIL, appTask, taskQueueName, STaskFinishType.FAIL(CErrorCode.CODE_TASK_VERIFY_DIR_NULL.intErrorCode2taskException()))
            return
        }
        if (appTask.filePathNameExt.isFileNotExist()) {
            UtilKLogWrapper.e(TAG, "startVerify: download file fail")
            onTaskFinished(ATaskState.STATE_VERIFY_FAIL, appTask, taskQueueName, STaskFinishType.FAIL(CErrorCode.CODE_TASK_VERIFY_FILE_NOT_EXIST.intErrorCode2taskException()))
            return
        }
        if (appTask.taskVerifyFileMd5.isNotEmpty()) {
            val fileMd5Remote = appTask.taskVerifyFileMd5
            val fileMd5Locale = appTask.filePathNameExt.strFilePath2file().file2strMd5Hex_use_ofStream()//取文件的MD5值
            if (fileMd5Remote != fileMd5Locale/*!TextUtils.equals(appTask.taskVerifyFileMd5, apkFileMd5Locale)*/) {
                UtilKLogWrapper.e(TAG, "startVerify: download file fail")
                onTaskFinished(ATaskState.STATE_VERIFY_FAIL, appTask, taskQueueName, STaskFinishType.FAIL(CErrorCode.CODE_TASK_VERIFY_MD5_FAIL.intErrorCode2taskException()))
                return
            }
        }

        onTaskFinished(ATaskState.STATE_VERIFY_SUCCESS, appTask, taskQueueName, STaskFinishType.SUCCESS)
    }
}