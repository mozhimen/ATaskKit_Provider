package com.mozhimen.netk.app.annors

import androidx.annotation.IntDef
import com.mozhimen.netk.app.task.cons.CNetKAppTaskState

/**
 * @ClassName ANetKAppTaskState
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/1/2 22:54
 * @Version 1.0
 */
@IntDef(
    value = [
        CNetKAppTaskState.STATE_TASK_CREATE,
        CNetKAppTaskState.STATE_TASKING,
        CNetKAppTaskState.STATE_TASK_PAUSE,
        CNetKAppTaskState.STATE_TASK_CANCEL,
        CNetKAppTaskState.STATE_TASK_SUCCESS,
        CNetKAppTaskState.STATE_TASK_FAIL,
        CNetKAppTaskState.STATE_TASK_UNAVAILABLE,
        CNetKAppTaskState.STATE_TASK_UPDATE
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class ANetKAppTaskState
