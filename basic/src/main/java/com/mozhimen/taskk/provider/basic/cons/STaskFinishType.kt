package com.mozhimen.taskk.provider.basic.cons

import com.mozhimen.taskk.provider.basic.impls.TaskException


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