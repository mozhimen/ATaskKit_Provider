package com.mozhimen.taskk.provider.apk.impls

import android.annotation.SuppressLint
import android.util.Log
import com.mozhimen.installk.manager.commons.IInstallKReceiverProxy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.wrapper.UtilKAppInstall
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.apk.impls.interceptors.TaskInterceptorApk
import com.mozhimen.taskk.provider.basic.annors.AState
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType

/**
 * @ClassName NetKAppInstallProviderDefault
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
class TaskInstallApk(
    taskManager: ATaskManager,
    iTaskLifecycle: ITaskLifecycle,
) : ATaskInstall(taskManager, iTaskLifecycle) {

    protected var _iInstallKReceiverProxy: IInstallKReceiverProxy? = null

    fun setInstallKReceiverProxy(iInstallKReceiverProxy: IInstallKReceiverProxy): ATaskInstall {
        _iInstallKReceiverProxy = iInstallKReceiverProxy
        return this
    }

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    @OptIn(OApiInit_InApplication::class)
    @SuppressLint("MissingSuperCall")
    @OPermission_REQUEST_INSTALL_PACKAGES
    override fun taskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        if (!appTask.canTaskInstall(_taskManager, taskQueueName)) {
            UtilKLogWrapper.e(TAG, "install: the task hasn't unzip or verify success")
//            onTaskFinished(ATaskState.STATE_INSTALL_FAIL, STaskFinishType.FAIL(CErrorCode.CODE_TASK_INSTALL_HAST_VERIFY_OR_UNZIP.intErrorCode2taskException()), appTask)
            return
        }
//        super.taskStart(appTask)
        if (appTask.apkPackageName.isNotEmpty()) {
            _iInstallKReceiverProxy?.addPackageName(appTask.apkPackageName)
        }
        if (appTask.taskUnzipEnable && appTask.taskUnzipFilePath.isNotEmpty()) {
            UtilKLogWrapper.d(TAG, "taskStart: appTask.taskUnzipEnable true appTask.taskUnzipFilePath ${appTask.taskUnzipFilePath}")
            UtilKAppInstall.install_ofView(appTask.taskUnzipFilePath)
        } else {
            UtilKLogWrapper.d(TAG, "taskStart: appTask.taskUnzipEnable false")
            UtilKAppInstall.install_ofView(appTask.filePathNameExt)
        }
    }

    override fun onTaskFinished(taskState: Int, appTask: AppTask, taskQueueName: String, finishType: STaskFinishType) {
        Log.d(TAG, "onTaskFinished: taskState $taskState taskQueueName $taskQueueName appTask $appTask ")
        if (finishType is STaskFinishType.SUCCESS) {
            if (TaskInterceptorApk.isAutoDeleteOrgFiles()) {
                TaskInterceptorApk.deleteOrgFiles(appTask)
            }
        }
        super.onTaskFinished(taskState, appTask, taskQueueName, finishType)
    }
}