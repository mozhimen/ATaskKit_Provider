package com.mozhimen.taskk.task.provider.commons

import com.mozhimen.taskk.task.provider.cons.STaskFinishType
import com.mozhimen.taskk.task.provider.db.AppTask
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName ITaskProviders
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
interface ITaskProviderSet<T : ITaskProvider> : ITaskProvider {
    val providerDefault: T
    val providers: ConcurrentHashMap<String, T>

    fun addProvider(provider: T, isOverwrite: Boolean) {
        provider.getSupportFileExtensions().forEach { fileExt ->
            if (!providers.containsKey(fileExt)) {
                providers[fileExt] = provider
            } else if (isOverwrite) {
                providers[fileExt] = provider
            }
        }
    }

    fun getProvider(fileExt: String): T? =
        providers[fileExt]

    override fun getSupportFileExtensions(): List<String> {
        return providers.keys().toList()
    }

    ////////////////////////////////////////////////////////////////////

    override fun taskStart(appTask: AppTask) {
        providers[appTask.fileExt]?.taskStart(appTask)
    }

    override fun taskPause(appTask: AppTask) {
        providers[appTask.fileExt]?.taskPause(appTask)
    }

    override fun taskResume(appTask: AppTask) {
        providers[appTask.fileExt]?.taskResume(appTask)
    }

    override fun taskCancel(appTask: AppTask) {
        providers[appTask.fileExt]?.taskCancel(appTask)
    }

    ////////////////////////////////////////////////////////////////////

    override fun onTaskStarted(taskState: Int, appTask: AppTask) {
        super.onTaskStarted(taskState, appTask)
        providers[appTask.fileExt]?.onTaskStarted(taskState,appTask)
    }

    override fun onTaskPaused(taskState: Int, appTask: AppTask) {
        super.onTaskPaused(taskState, appTask)
        providers[appTask.fileExt]?.onTaskPaused(taskState,appTask)
    }

    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        super.onTaskFinished(taskState, finishType, appTask)
        providers[appTask.fileExt]?.onTaskFinished(taskState, finishType, appTask)
    }
}