package com.mozhimen.taskk.provider.apk

import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.installk.manager.commons.IInstallKReceiverProxy
import com.mozhimen.taskk.provider.apk.interfaces.ITaskProviderInterceptorApk
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderInstall
import com.mozhimen.taskk.provider.basic.cons.CErrorCode
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.intErrorCode2taskException
import com.mozhimen.taskk.provider.install.TaskProviderSetInstall

/**
 * @ClassName TaskProviderSetInstallApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
class TaskProviderSetInstallApk(
    providerDefault: ATaskProviderInstall,
    private val _iInstallKReceiverProxy: IInstallKReceiverProxy?,
    private val _iTaskProviderInterceptor: ITaskProviderInterceptorApk?
) : TaskProviderSetInstall(providerDefault) {

    override fun taskStart(appTask: AppTask) {
        if (!appTask.canTaskInstall()) {
            UtilKLogWrapper.e(TAG, "install: the task hasn't unzip or verify success")
            /**
             * Net
             */
            onTaskFinished(CTaskState.STATE_INSTALL_FAIL, STaskFinishType.FAIL(CErrorCode.CODE_TASK_INSTALL_HAST_VERIFY_OR_UNZIP.intErrorCode2taskException()), appTask)
            return
        }
        if (appTask.apkPackageName.isNotEmpty()) {
            _iInstallKReceiverProxy?.addPackageName(appTask.apkPackageName)
        }
        super.taskStart(appTask)
    }

    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        when (finishType) {
            STaskFinishType.SUCCESS -> {
                if (_iTaskProviderInterceptor != null && _iTaskProviderInterceptor.isAutoDeleteOrgFiles()) {
                    _iTaskProviderInterceptor.deleteOrgFiles()
                }
            }

            STaskFinishType.CANCEL -> {
                _iTaskProviderInterceptor?.deleteOrgFiles()
            }

            else -> {}
        }
        super.onTaskFinished(taskState, finishType, appTask)
    }
}