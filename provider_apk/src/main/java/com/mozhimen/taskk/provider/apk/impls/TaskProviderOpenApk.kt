package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.basick.lintk.optins.permission.OPermission_QUERY_ALL_PACKAGES
import com.mozhimen.basick.utilk.android.app.UtilKApplicationWrapper
import com.mozhimen.basick.utilk.android.content.UtilKContextStart
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderOpen
import com.mozhimen.taskk.provider.basic.cons.CErrorCode
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.impls.exception2taskException
import com.mozhimen.taskk.provider.basic.impls.intErrorCode2taskException
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderLifecycle

/**
 * @ClassName TaskProviderOpenApk
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/8/21 21:55
 * @Version 1.0
 */
class TaskProviderOpenApk(iTaskProviderLifecycle: ITaskProviderLifecycle) : ATaskProviderOpen(iTaskProviderLifecycle) {
    override fun getSupportFileExtensions(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    @OptIn(OPermission_QUERY_ALL_PACKAGES::class)
    override fun taskStart(appTask: AppTask) {
        super.taskStart(appTask)
        try {
            val boolean = UtilKContextStart.startContext_ofPackageName(UtilKApplicationWrapper.instance.get(), appTask.apkPackageName)
            if (boolean) {
                onTaskFinished(CTaskState.STATE_OPEN_SUCCESS, STaskFinishType.SUCCESS, appTask)
            } else {
                onTaskFinished(CTaskState.STATE_OPEN_FAIL, STaskFinishType.FAIL(CErrorCode.CODE_TASK_OPEN_FAIL.intErrorCode2taskException()), appTask)
            }
        } catch (e: Exception) {
            onTaskFinished(CTaskState.STATE_OPEN_FAIL, STaskFinishType.FAIL(e.exception2taskException()), appTask)
        }
    }
}