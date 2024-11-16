package com.mozhimen.taskk.provider.basic.annors

import android.util.Log
import androidx.annotation.IntDef
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.commons.IUtilK
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
    companion object : IUtilK {
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

        fun canTaskExecute(taskState: Int, currentTaskCode: Int, preTaskCode: Int?): Boolean =
            taskState in (preTaskCode?.let { it + STATE_TASK_SUCCESS } ?: (currentTaskCode + STATE_TASK_CREATE))..(currentTaskCode + STATE_TASK_PAUSE)

        fun canTaskStart(taskState: Int): Boolean =
            taskState in STATE_TASK_CREATE..STATE_TASK_UPDATE

        fun canTaskFinish(taskState: Int): Boolean =
            taskState in STATE_TASK_CANCEL..STATE_TASK_FAIL

        /////////////////////////////////////////////////////////////////////

        @OptIn(OApiInit_InApplication::class)
        fun isTaskProcess(taskState: Int): Boolean =
            !isTaskCreate(taskState) &&
                    !isTaskUpdate(taskState) &&
                    !isTaskUnAvailable(taskState) &&
                    !isTaskCancel(taskState) &&
                    !isTaskFail(taskState) &&
                    !isTaskSuccess(taskState)

        @OptIn(OApiInit_InApplication::class)
        fun isTaskProcess(taskState: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
            !isTaskCreate(taskState) &&
                    !isTaskUpdate(taskState) &&
                    !isTaskUnAvailable(taskState) &&
                    !isTaskCancel(taskState) &&
                    !isTaskFail(taskState) &&
                    !isTaskSuccess(taskState, taskManager, fileExt, taskNodeQueueName)

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

        fun isTaskSuccess(state: Int): Boolean =
            state == STATE_TASK_SUCCESS

        @OptIn(OApiInit_InApplication::class)
        fun isTaskSuccess(state: Int, taskManager: ATaskManager, @AFileExt fileExt: String, @ATaskNodeQueueName taskNodeQueueName: String): Boolean =
            (isTaskSuccess(state) || run {
                val lastTaskCode: Int = taskManager.getLastTaskNode_ofTaskNodeQueue(fileExt, taskNodeQueueName)?.taskName?.taskName2taskState() ?: return@run false
                val taskCode: Int = ATaskState.getTaskCode(state)
                val stateCode: Int = getStateCode(state)
                Log.d(TAG, "isTaskSuccess: state $state lastTaskCode $lastTaskCode taskCode $taskCode stateCode $stateCode")
                lastTaskCode == taskCode && stateCode == STATE_TASK_SUCCESS
            }).also { Log.d(TAG, "isTaskSuccess: $it") }

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
