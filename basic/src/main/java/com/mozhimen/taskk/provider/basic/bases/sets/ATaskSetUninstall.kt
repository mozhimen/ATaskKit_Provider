package com.mozhimen.taskk.provider.basic.bases.sets

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskSet
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall

/**
 * @ClassName ITaskProviderSetUninstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
abstract class ATaskSetUninstall : ATaskSet<ATaskUninstall>() {
    override fun getTaskName(): String {
        return ATaskName.TASK_UNINSTALL
    }
}