package com.mozhimen.taskk.provider.basic.bases.sets

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskProviderSet
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderOpen

/**
 * @ClassName ITaskProviderOpen
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
abstract class ATaskProviderSetOpen : ATaskProviderSet<ATaskProviderOpen>() {
    override fun getTaskName(): String {
        return ATaskName.TASK_OPEN
    }
}