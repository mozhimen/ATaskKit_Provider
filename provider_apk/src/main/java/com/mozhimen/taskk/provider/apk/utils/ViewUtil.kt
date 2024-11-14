package com.mozhimen.taskk.provider.apk.utils

import android.content.Context
import android.view.View
import com.mozhimen.kotlin.elemk.commons.IAB_Listener
import com.mozhimen.kotlin.elemk.commons.I_AListener
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName NetKAppViewUtil
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/12/6 9:58
 * @Version 1.0
 */
object ViewUtil {
    @OptIn(OApiInit_InApplication::class)
    @JvmStatic
    fun generateViewLongClickOfAppTask(
        view: View,
        taskManager: ATaskManager,
        @ATaskQueueName taskQueueName_process: String,
        @ATaskQueueName taskQueueName_success: String,
        onGetAppTask: I_AListener<AppTask>,
        onTaskCancel: IAB_Listener<Context, AppTask>,
    ) {
        view.setOnLongClickListener {
            val appTask = onGetAppTask.invoke()
            if (appTask.canTaskCancel(taskManager, taskQueueName_process)) {
                onTaskCancel.invoke(it.context, appTask)
            }
            true
        }
    }

    @OptIn(OApiInit_InApplication::class)
    @JvmStatic
    fun generateViewClickOfAppTask(
        view: View,
        taskManager: ATaskManager,
        @ATaskQueueName taskQueueName_process: String,
        @ATaskQueueName taskQueueName_success: String,
        onGetAppTask: I_AListener<AppTask>,
        onTaskStart: IAB_Listener<Context, AppTask>,
        onTaskOpen: IAB_Listener<Context, AppTask>,
        onTaskPause: IAB_Listener<Context, AppTask>,
        onTaskResume: IAB_Listener<Context, AppTask>,
        onTaskInstall: IAB_Listener<Context, AppTask>,
        onTaskCancel: IAB_Listener<Context, AppTask>,
    ) {
        view.setOnClickListener {
            val appTask: AppTask = onGetAppTask.invoke()
            when {
                appTask.isTaskCreateOrUpdate() -> {
                    onTaskStart.invoke(it.context, appTask)
                }

                appTask.isTaskSuccess(taskManager, taskQueueName_success) -> {
                    onTaskOpen.invoke(it.context, appTask)
                }

                appTask.isAnyTasking() -> {
                    onTaskPause.invoke(it.context, appTask)
                }

                appTask.isAnyTaskPause() -> {
                    onTaskResume.invoke(it.context, appTask)
                }

                appTask.isTaskSuccess(taskManager, taskQueueName_process)/*ATaskState.STATE_UNZIP_SUCCESS, ATaskState.STATE_INSTALLING*/ -> {
                    onTaskInstall.invoke(it.context, appTask)
                }

                else -> {
                }
            }
        }
        generateViewLongClickOfAppTask(
            view,
            taskManager,
            taskQueueName_process,
            taskQueueName_success,
            onGetAppTask,
            onTaskCancel
        )
    }
}