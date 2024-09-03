package com.mozhimen.taskk.provider.apk.impls

import android.annotation.SuppressLint
import com.mozhimen.installk.manager.commons.IInstallKReceiverProxy
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.wrapper.UtilKAppInstall
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle

/**
 * @ClassName NetKAppInstallProviderDefault
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/6/21
 * @Version 1.0
 */
class TaskInstallApk(
    iTaskLifecycle: ITaskLifecycle,
) : ATaskInstall(iTaskLifecycle) {

    protected var _iInstallKReceiverProxy: IInstallKReceiverProxy? = null

    fun setInstallKReceiverProxy(iInstallKReceiverProxy: IInstallKReceiverProxy): ATaskInstall {
        _iInstallKReceiverProxy = iInstallKReceiverProxy
        return this
    }

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    @SuppressLint("MissingSuperCall")
    @OPermission_REQUEST_INSTALL_PACKAGES
    override fun taskStart(appTask: AppTask) {
        if (!appTask.canTaskInstall()) {
            UtilKLogWrapper.e(TAG, "install: the task hasn't unzip or verify success")
//            onTaskFinished(CTaskState.STATE_INSTALL_FAIL, STaskFinishType.FAIL(CErrorCode.CODE_TASK_INSTALL_HAST_VERIFY_OR_UNZIP.intErrorCode2taskException()), appTask)
            return
        }
        if (appTask.apkPackageName.isNotEmpty()) {
            _iInstallKReceiverProxy?.addPackageName(appTask.apkPackageName)
        }
        if (appTask.taskUnzipEnable) {
            UtilKLogWrapper.d(TAG, "taskStart: appTask.taskUnzipEnable true")
            UtilKAppInstall.install_ofView(appTask.taskUnzipFilePath)
        } else {
            UtilKLogWrapper.d(TAG, "taskStart: appTask.taskUnzipEnable false")
            UtilKAppInstall.install_ofView(appTask.filePathNameExt)
        }
//        super.taskStart(appTask)
    }
}