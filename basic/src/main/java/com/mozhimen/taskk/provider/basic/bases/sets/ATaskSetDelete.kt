package com.mozhimen.taskk.provider.basic.bases.sets

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.ATaskSet
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDelete
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName ITaskProviderSetUninstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
abstract class ATaskSetDelete(taskManager:ATaskManager) : ATaskSet<ATaskDelete>(taskManager) {
    fun taskDeleteAll(fileExt: String, appTasks: List<AppTask>, @ATaskNodeQueueName taskNodeQueueName: String) {
        getProvider(fileExt)?.taskDeleteAll(appTasks, taskNodeQueueName)
    }

    override fun getTaskName(): String {
        return ATaskName.TASK_DELETE
    }
}