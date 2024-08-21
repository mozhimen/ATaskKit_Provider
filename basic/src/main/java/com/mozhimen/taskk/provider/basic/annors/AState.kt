package com.mozhimen.taskk.provider.basic.annors

import androidx.annotation.IntDef
import com.mozhimen.taskk.provider.basic.cons.CState

/**
 * @ClassName ANetKAppTaskState
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/1/2 22:54
 * @Version 1.0
 */
@IntDef(
    value = [
        CState.STATE_TASK_CREATE,
        CState.STATE_TASKING,
        CState.STATE_TASK_PAUSE,
        CState.STATE_TASK_CANCEL,
        CState.STATE_TASK_SUCCESS,
        CState.STATE_TASK_FAIL,
        CState.STATE_TASK_UNAVAILABLE,
        CState.STATE_TASK_UPDATE
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class AState
