package com.mozhimen.taskk.provider.basic.bases.sets

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.bases.ATaskSet
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUnzip

/**
 * @ClassName ITaskProviderSetUnzip
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
abstract class ATaskSetUnzip(taskManager: ATaskManagerProvider) : ATaskSet<ATaskUnzip>(taskManager) {
    override fun getTaskName(): String {
        return ATaskName.TASK_UNZIP
    }
}