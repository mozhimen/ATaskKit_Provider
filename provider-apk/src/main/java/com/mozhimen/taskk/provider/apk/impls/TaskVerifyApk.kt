package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.java.io.file2strMd5Hex_use_ofStream
import com.mozhimen.kotlin.utilk.kotlin.isFileNotExist
import com.mozhimen.kotlin.utilk.kotlin.strFilePath2file
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.apk.impls.interceptors.TaskInterceptorApk
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
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
class TaskVerifyApk(taskManager: ATaskManagerProvider, iTaskLifecycle: ITaskLifecycle) : ATaskVerify(taskManager,iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    //////////////////////////////////////////////////////////////////

    override fun taskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        if (appTask.atTaskUnzip()) {
            UtilKLogWrapper.d(TAG, "verify: the task already verify")
            return
        }
        UtilKLogWrapper.d(TAG, "verify: filePathNameExt ${appTask.filePathNameExt} fileNameExt ${appTask.fileNameExt}")

        super.taskStart(appTask, taskNodeQueueName)

        if (appTask.taskVerifyEnable) {//如果文件没有MD5值或者为空，则不校验 直接去安装
            startVerify(appTask, taskNodeQueueName)
        } else {
            onTaskFinished(ATaskState.STATE_VERIFY_SUCCESS, appTask, taskNodeQueueName, STaskFinishType.SUCCESS)
        }
    }

    override fun taskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        if (appTask.isTaskVerifySuccess()) {
            TaskInterceptorApk.deleteOrgFiles(appTask)
            onTaskFinished(ATaskState.STATE_VERIFY_CANCEL, appTask, taskNodeQueueName, STaskFinishType.CANCEL)
        }
    }

    override fun canTaskCancel(appTask: AppTask, taskNodeQueueName: String): Boolean {
        return true
    }

    //////////////////////////////////////////////////////////////////

    private fun startVerify(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        if (appTask.filePathNameExt.isEmpty()) {
            UtilKLogWrapper.e(TAG, "startVerify: getFilesDownloadsDir is null")
            onTaskFinished(ATaskState.STATE_VERIFY_FAIL, appTask, taskNodeQueueName, STaskFinishType.FAIL(CErrorCode.CODE_TASK_VERIFY_DIR_NULL.intErrorCode2taskException()))
            return
        }
        if (appTask.filePathNameExt.isFileNotExist()) {
            UtilKLogWrapper.e(TAG, "startVerify: download file fail")
            onTaskFinished(ATaskState.STATE_VERIFY_FAIL, appTask, taskNodeQueueName, STaskFinishType.FAIL(CErrorCode.CODE_TASK_VERIFY_FILE_NOT_EXIST.intErrorCode2taskException()))
            return
        }
        if (appTask.taskVerifyFileMd5.isNotEmpty()) {
            val fileMd5Remote = appTask.taskVerifyFileMd5
            val fileMd5Locale = appTask.filePathNameExt.strFilePath2file().file2strMd5Hex_use_ofStream()//取文件的MD5值
            if (fileMd5Remote != fileMd5Locale/*!TextUtils.equals(appTask.taskVerifyFileMd5, apkFileMd5Locale)*/) {
                UtilKLogWrapper.e(TAG, "startVerify: download file fail")
                onTaskFinished(ATaskState.STATE_VERIFY_FAIL, appTask, taskNodeQueueName, STaskFinishType.FAIL(CErrorCode.CODE_TASK_VERIFY_MD5_FAIL.intErrorCode2taskException()))
                return
            }
        }

        onTaskFinished(ATaskState.STATE_VERIFY_SUCCESS, appTask, taskNodeQueueName, STaskFinishType.SUCCESS)
    }
}