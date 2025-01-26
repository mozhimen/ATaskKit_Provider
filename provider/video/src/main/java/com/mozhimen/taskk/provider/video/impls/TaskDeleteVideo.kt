package com.mozhimen.taskk.provider.video.impls

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.taskk.provider.video.interceptors.TaskInterceptorVideo
import com.mozhimen.taskk.provider.basic.annors.AState
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDelete
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import com.mozhimen.taskk.provider.video.cons.CExt

/**
 * @ClassName TaskDeleteApk
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/9/22 23:45
 * @Version 1.0
 */
class TaskDeleteVideo(taskManager: ATaskManagerProvider, iTaskLifecycle: ITaskLifecycle?) : ATaskDelete(taskManager, iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf(
            CExt.EXT_MP4,
            CExt.EXT_AVI,
            CExt.EXT_MKV,
            CExt.EXT_MOV,
            CExt.EXT_WMV,
            CExt.EXT_FLV,
            CExt.EXT_WEBM,
            CExt.EXT_3GP,
            CExt.EXT_MPEG,
            CExt.EXT_VOB,
            CExt.EXT_TS,
            CExt.EXT_M2TS,
            CExt.EXT_BDMV
        )
    }

    @OptIn(OApiInit_InApplication::class)
    override fun taskStart(appTask: AppTask, taskNodeQueueName: String) {
        if (!appTask.canTaskDelete(_taskManager, taskNodeQueueName)) {
            UtilKLogWrapper.e(TAG, "install: the task hasn't download success appTask $appTask")
//            onTaskFinished(ATaskState.STATE_INSTALL_FAIL, STaskFinishType.FAIL(CErrorCode.CODE_TASK_INSTALL_HAST_VERIFY_OR_UNZIP.intErrorCode2taskException()), appTask)
            return
        }
        super.taskStart(appTask, taskNodeQueueName)
        startDelete(appTask, taskNodeQueueName)
    }

    private fun startDelete(appTask: AppTask, taskNodeQueueName: String) {
        //删除文件
        TaskInterceptorVideo.deleteOrgFiles(appTask)
        //重置状态
        if (appTask.taskStateInit == AState.STATE_TASK_UPDATE) {
            appTask.taskStateInit = AState.STATE_TASK_CREATE
        }
        onTaskFinished(ATaskState.STATE_DELETE_SUCCESS, appTask, taskNodeQueueName, STaskFinishType.SUCCESS)
    }
}