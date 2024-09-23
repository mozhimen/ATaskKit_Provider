package com.mozhimen.taskk.provider.basic.annors

import androidx.annotation.IntDef
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.taskk.provider.basic.bases.ATaskManager

/**
 * @ClassName ANetKAppTaskState
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/1/2 22:54
 * @Version 1.0
 */
@Target(AnnotationTarget.TYPE)
@IntDef(
    value = [
        AState.STATE_TASK_CREATE,
        AState.STATE_TASKING,
        AState.STATE_TASK_PAUSE,
        AState.STATE_TASK_CANCEL,
        AState.STATE_TASK_SUCCESS,
        AState.STATE_TASK_FAIL,
        AState.STATE_TASK_UNAVAILABLE,
        AState.STATE_TASK_UPDATE
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class AState {
    companion object {
        //任务
        const val STATE_TASK_CREATE = 0//STATE_NOT_INSTALLED = 0//任务创建 未安装 处于未下载，
        const val STATE_TASK_UPDATE = 1//需要更新状态
        const val STATE_TASK_UNAVAILABLE = 6//不可达状态

        //    const val STATE_TASK_WAIT = 1//STATE_PENDING = 3//任务等待
        const val STATE_TASKING = 2//任务中
        const val STATE_TASK_PAUSE = 3//任务暂停

        //    const val STATE_TASK_WAIT_CANCEL = 2//STATE_PENDING_CANCELED = 4//取消等待中
        const val STATE_TASK_CANCEL = 7//取消任务
        const val STATE_TASK_SUCCESS = 8//任务成功
        const val STATE_TASK_FAIL = 9//任务失败

        /////////////////////////////////////////////////////////////////////

        fun getStateCode(taskState: Int): @AState Int =
            taskState % 10

        /////////////////////////////////////////////////////////////////////

        @OptIn(OApiInit_InApplication::class)
        fun isTaskProcess(taskState: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): Boolean =
            !isTaskCreate(taskState) &&
                    !isTaskUpdate(taskState) &&
                    !isTaskUnAvailable(taskState) &&
                    !isTaskCancel(taskState) &&
                    !isTaskFail(taskState) &&
                    !isTaskSuccess(taskState, taskManager, fileExt, taskQueueName)

        fun isTaskCreate(state: Int): Boolean =
            state == STATE_TASK_CREATE

        fun isTaskUpdate(state: Int): Boolean =
            state == STATE_TASK_UPDATE

        fun isTaskCreateOrUpdate(state: Int): Boolean =
            isTaskCreate(state) or isTaskUpdate(state)

        fun isTaskUnAvailable(state: Int): Boolean =
            state == STATE_TASK_UNAVAILABLE

        fun isTaskCancel(state: Int): Boolean =
            state == STATE_TASK_CANCEL

        @OptIn(OApiInit_InApplication::class)
        fun isTaskSuccess(state: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): Boolean =
            state == STATE_TASK_SUCCESS || run {
                val lastTaskCode: Int = taskManager.getLastTaskName_ofTaskQueue(fileExt, taskQueueName)?.taskName2taskState() ?: return@run false
                val taskCode: Int = ATaskState.getTaskCode(state)
                val stateCode: Int = AState.getStateCode(state)
                lastTaskCode == taskCode && stateCode == STATE_TASK_SUCCESS
            }

        fun isTaskFail(state: Int): Boolean =
            state == STATE_TASK_FAIL

        /////////////////////////////////////////////////////////////////////

        fun isAnyTasking(state: Int): Boolean =
            getStateCode(state) == STATE_TASKING

        fun isAnyTaskPause(state: Int): Boolean =
            getStateCode(state) == STATE_TASK_PAUSE

        fun isAnyTaskSuccess(state: Int): Boolean =
            getStateCode(state) == STATE_TASK_SUCCESS

        fun isAnyTaskCancel(state: Int): Boolean =
            getStateCode(state) == STATE_TASK_CANCEL

        fun isAnyTaskFail(state: Int): Boolean =
            getStateCode(state) == STATE_TASK_FAIL

        fun isAnyTaskResult(state: Int): Boolean =
            isAnyTaskFail(state) || isAnyTaskSuccess(state) || isAnyTaskCancel(state)

        /////////////////////////////////////////////////////////////////////

//      @JvmStatic
//      fun isTaskWait(state: Int): Boolean =
//          getStateCode(state) == STATE_TASK_WAIT
    }
}
