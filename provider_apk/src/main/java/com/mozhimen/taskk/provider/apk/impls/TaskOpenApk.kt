package com.mozhimen.taskk.provider.apk.impls

import android.annotation.SuppressLint
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_QUERY_ALL_PACKAGES
import com.mozhimen.kotlin.utilk.android.app.UtilKApplicationWrapper
import com.mozhimen.kotlin.utilk.android.content.UtilKContextStart
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskOpen
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle

/**
 * @ClassName TaskProviderOpenApk
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/8/21 21:55
 * @Version 1.0
 */
class TaskOpenApk(iTaskLifecycle: ITaskLifecycle) : ATaskOpen(iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    @SuppressLint("MissingSuperCall")
    @OptIn(OPermission_QUERY_ALL_PACKAGES::class)
    override fun taskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        try {
            val boolean = UtilKContextStart.startContext_ofPackageName(UtilKApplicationWrapper.instance.get(), appTask.apkPackageName)
            if (boolean) {
//                onTaskFinished(CTaskState.STATE_OPEN_SUCCESS, STaskFinishType.SUCCESS, appTask)
            } else {
//                onTaskFinished(CTaskState.STATE_OPEN_FAIL, STaskFinishType.FAIL(CErrorCode.CODE_TASK_OPEN_FAIL.intErrorCode2taskException()), appTask)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "taskStart: ", e)
//            onTaskFinished(CTaskState.STATE_OPEN_FAIL, STaskFinishType.FAIL(e.exception2taskException()), appTask)
        }
    }
}