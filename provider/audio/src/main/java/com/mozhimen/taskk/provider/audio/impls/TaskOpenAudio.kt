package com.mozhimen.taskk.provider.audio.impls

import android.annotation.SuppressLint
import com.mozhimen.kotlin.utilk.android.app.UtilKApplicationWrapper
import com.mozhimen.kotlin.utilk.android.content.UtilKContextStart
import com.mozhimen.kotlin.utilk.android.content.UtilKIntentGet
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.kotlin.strFilePath2uri
import com.mozhimen.taskk.provider.audio.cons.CExt
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskOpen
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName TaskOpenAudio
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/1/23
 * @Version 1.0
 */
class TaskOpenAudio(taskManager: ATaskManagerProvider, iTaskLifecycle: ITaskLifecycle) : ATaskOpen(taskManager, iTaskLifecycle) {

    override fun getSupportFileExts(): List<String> {
        return listOf(
            CExt.EXT_MP3,
            CExt.EXT_MP3,
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
            CExt.EXT_ASF,
            CExt.EXT_M2TS,
            CExt.EXT_BDMV,
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun taskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        try {
            val intent = appTask.filePathNameExt.strFilePath2uri()?.let { UtilKIntentGet.getIntent_ACTION_VIEW_TYPE_AUDIO(it,UtilKApplicationWrapper.instance.get()) }
            val boolean = if (intent != null) {
                UtilKContextStart.startContext(UtilKApplicationWrapper.instance.get(), intent)
            } else false
            UtilKLogWrapper.d(TAG, "taskStart: open res $boolean")
//            if (boolean) {
//                onTaskFinished(ATaskState.STATE_OPEN_SUCCESS, STaskFinishType.SUCCESS, appTask)
//            } else {
//                onTaskFinished(ATaskState.STATE_OPEN_FAIL, STaskFinishType.FAIL(CErrorCode.CODE_TASK_OPEN_FAIL.intErrorCode2taskException()), appTask)
//            }
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "taskStart: ", e)
//            onTaskFinished(ATaskState.STATE_OPEN_FAIL, STaskFinishType.FAIL(e.exception2taskException()), appTask)
        }
    }
}