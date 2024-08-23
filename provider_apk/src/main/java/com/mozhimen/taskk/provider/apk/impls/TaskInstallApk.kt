package com.mozhimen.taskk.provider.apk.impls

import android.annotation.SuppressLint
import com.mozhimen.basick.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.wrapper.UtilKAppInstall
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.apk.interfaces.ITaskProviderInterceptorApk
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
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

    protected var _iTaskProviderInterceptor: ITaskProviderInterceptorApk? = null

    fun setTaskProviderInterceptor(iTaskProviderInterceptor: ITaskProviderInterceptorApk): TaskInstallApk {
        _iTaskProviderInterceptor = iTaskProviderInterceptor
        return this
    }

    override fun getSupportFileExtensions(): List<String> {
        return listOf(CExt.EXT_APK)
    }

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
            UtilKAppInstall.install_ofView(appTask.taskUnzipFilePath)
        } else {
            UtilKAppInstall.install_ofView(appTask.filePathNameExt)
        }
        super.taskStart(appTask)
    }

    @SuppressLint("MissingSuperCall")
    override fun taskCancel(appTask: AppTask) {
    }

    @SuppressLint("MissingSuperCall")
    override fun taskPause(appTask: AppTask) {
    }

    @SuppressLint("MissingSuperCall")
    override fun taskResume(appTask: AppTask) {
    }

    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        when (finishType) {
            STaskFinishType.SUCCESS -> {
                if (_iTaskProviderInterceptor?.isAutoDeleteOrgFiles() == true) {
                    _iTaskProviderInterceptor?.deleteOrgFiles(appTask)
                }
            }

            else -> {}
        }
        super.onTaskFinished(taskState, finishType, appTask)
    }
}