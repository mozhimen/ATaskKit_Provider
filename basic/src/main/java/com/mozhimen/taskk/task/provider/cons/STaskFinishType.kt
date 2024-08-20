package com.mozhimen.taskk.task.provider.cons

import com.mozhimen.taskk.task.provider.impls.TaskException

/**
 * @ClassName ENetKAppFinish
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/10 17:41
 * @Version 1.0
 */
sealed class STaskFinishType {
    object SUCCESS : STaskFinishType()
    object CANCEL : STaskFinishType()
    data class FAIL(val exception: TaskException) : STaskFinishType()
}