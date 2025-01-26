package com.mozhimen.taskk.provider.basic.bases.sets

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.bases.ATaskSet
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskClose

/**
 * @ClassName ITaskProviderSetUninstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
abstract class ATaskSetClose(taskManager: ATaskManagerProvider) : ATaskSet<ATaskClose>(taskManager) {
    override fun getTaskName(): String {
        return ATaskName.TASK_CLOSE
    }
}