package com.mozhimen.taskk.provider.basic.bases

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.CallSuper
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName ITaskProviders
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
abstract class ATaskSet<T : ATask>(taskManager: ATaskManager) : ATask(taskManager, null) {
    abstract val providerDefaults: List<T>
    abstract val providers: ConcurrentHashMap<String, T>

    ////////////////////////////////////////////////////////////////////

    @CallSuper
    override fun init(context: Context) {
        if (!hasInit()) {
            super.init(context)
            for ((_, value) in providers) {
                if (!value.hasInit()) {
                    value.init(context)
                }
            }
        }
    }

    fun addProvider(context: Context, provider: T, isOverwrite: Boolean) {
        provider.getSupportFileExts().forEach { fileExt ->
            if (!providers.containsKey(fileExt)) {
                providers[fileExt] = provider.apply { init(context) }
            } else if (isOverwrite) {
                providers[fileExt] = provider.apply { init(context) }
            }
        }
    }

    fun getProvider(fileExt: String): T? =
        providers[fileExt]

    ////////////////////////////////////////////////////////////////////

    override fun getSupportFileTasks(): Map<String, T> {
        return providers
    }

    override fun getSupportFileExts(): List<String> {
        return providers.keys().toList()
    }

    ////////////////////////////////////////////////////////////////////

    @CallSuper
    override fun taskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        UtilKLogWrapper.d(TAG, "taskStart: ")
        getProvider(appTask.fileExt)?.taskStart(appTask, taskQueueName) ?: run { UtilKLogWrapper.e(TAG, "taskStart no provider") }
    }

    @CallSuper
    override fun taskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        UtilKLogWrapper.d(TAG, "taskPause: ")
        getProvider(appTask.fileExt)?.taskPause(appTask, taskQueueName) ?: run { UtilKLogWrapper.e(TAG, "taskPause no provider") }
    }

    @CallSuper
    override fun taskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        UtilKLogWrapper.d(TAG, "taskResume: ")
        getProvider(appTask.fileExt)?.taskResume(appTask, taskQueueName) ?: run { UtilKLogWrapper.e(TAG, "taskResume no provider") }
    }

    @CallSuper
    override fun taskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        UtilKLogWrapper.d(TAG, "taskCancel: ")
        getProvider(appTask.fileExt)?.taskCancel(appTask, taskQueueName) ?: run { UtilKLogWrapper.e(TAG, "taskCancel no provider") }
    }

    override fun canTaskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return (getProvider(appTask.fileExt)?.canTaskStart(appTask, taskQueueName) ?: false).also { UtilKLogWrapper.d(TAG, "canTaskStart $it") }
    }

    override fun canTaskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return (getProvider(appTask.fileExt)?.canTaskResume(appTask, taskQueueName) ?: false).also { UtilKLogWrapper.d(TAG, "canTaskResume $it") }
    }

    override fun canTaskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return (getProvider(appTask.fileExt)?.canTaskPause(appTask, taskQueueName) ?: false).also { UtilKLogWrapper.d(TAG, "canTaskPause $it") }
    }

    override fun canTaskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return (getProvider(appTask.fileExt)?.canTaskCancel(appTask, taskQueueName) ?: false).also { UtilKLogWrapper.d(TAG, "canTaskCancel $it") }
    }

    ////////////////////////////////////////////////////////////////////

    @SuppressLint("MissingSuperCall")
    @CallSuper
    override fun onTaskStarted(taskState: Int, appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        getProvider(appTask.fileExt)?.onTaskStarted(taskState, appTask, taskQueueName)
    }

    @SuppressLint("MissingSuperCall")
    @CallSuper
    override fun onTaskPaused(taskState: Int, appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        getProvider(appTask.fileExt)?.onTaskPaused(taskState, appTask, taskQueueName)
    }

    @SuppressLint("MissingSuperCall")
    @CallSuper
    override fun onTaskFinished(taskState: Int, appTask: AppTask, @ATaskQueueName taskQueueName: String, finishType: STaskFinishType) {
        getProvider(appTask.fileExt)?.onTaskFinished(taskState, appTask, taskQueueName, finishType)
    }
}