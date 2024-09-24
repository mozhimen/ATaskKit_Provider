package com.mozhimen.taskk.provider.apk.impls

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.apk.impls.interceptors.TaskInterceptorApk
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDelete
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle

/**
 * @ClassName TaskDeleteApk
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/9/22 23:45
 * @Version 1.0
 */
class TaskDeleteApk(taskManager: ATaskManager,iTaskLifecycle: ITaskLifecycle?) : ATaskDelete(taskManager,iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    @OptIn(OApiInit_InApplication::class)
    override fun taskStart(appTask: AppTask, taskQueueName: String) {
        if (!appTask.canTaskDelete(_taskManager,taskQueueName)) {
            UtilKLogWrapper.e(TAG, "install: the task hasn't download success appTask $appTask")
//            onTaskFinished(CTaskState.STATE_INSTALL_FAIL, STaskFinishType.FAIL(CErrorCode.CODE_TASK_INSTALL_HAST_VERIFY_OR_UNZIP.intErrorCode2taskException()), appTask)
            return
        }
        super.taskStart(appTask, taskQueueName)
        startDelete(appTask, taskQueueName)
    }

    private fun startDelete(appTask: AppTask, taskQueueName: String) {
        //删除文件
        TaskInterceptorApk.deleteOrgFiles(appTask)
        onTaskFinished(ATaskState.STATE_DELETE_SUCCESS, appTask, taskQueueName, STaskFinishType.SUCCESS)
    }
}