package com.mozhimen.taskk.provider.basic.bases.sets

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskProviderSet
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderUninstall

/**
 * @ClassName ITaskProviderSetUninstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
abstract class ATaskProviderSetUninstall : ATaskProviderSet<ATaskProviderUninstall>() {
    override fun getTaskName(): String {
        return ATaskName.TASK_UNINSTALL
    }
}