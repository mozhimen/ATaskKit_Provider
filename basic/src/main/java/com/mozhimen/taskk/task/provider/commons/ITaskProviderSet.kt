package com.mozhimen.taskk.task.provider.commons

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

    override fun onStarted(appTask: AppTask) {
        providers[appTask.fileExt]?.onTaskStarted(appTask)
    }

    override fun taskPause(appTask: AppTask) {
        providers[appTask.fileExt]?.onTaskPaused(appTask)
    }

    override fun onPaused(appTask: AppTask) {
        providers[appTask.fileExt]?.onTaskPaused(appTask)
    }

    override fun taskCancel(appTask: AppTask) {
        providers[appTask.fileExt]?.taskCancel(appTask)
    }

    override fun onCanceled(appTask: AppTask) {
        providers[appTask.fileExt]?.onTaskCanceled(appTask)
    }

    override fun onSucceeded(appTask: AppTask) {
        providers[appTask.fileExt]?.onTaskSucceeded(appTask)
    }

    override fun onFailed(appTask: AppTask) {
        providers[appTask.fileExt]?.onTaskFailed(appTask)
    }
}