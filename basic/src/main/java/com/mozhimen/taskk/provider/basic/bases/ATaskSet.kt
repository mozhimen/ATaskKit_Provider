package com.mozhimen.taskk.provider.basic.bases

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.CallSuper
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
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
abstract class ATaskSet<T : ATask> : ATask(null) {
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
    override fun taskStart(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "taskStart: ")
        getProvider(appTask.fileExt)?.taskStart(appTask) ?: run { UtilKLogWrapper.e(TAG,"taskStart no provider") }
    }

    @CallSuper
    override fun taskPause(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "taskPause: ")
        getProvider(appTask.fileExt)?.taskPause(appTask)?: run { UtilKLogWrapper.e(TAG,"taskPause no provider") }
    }

    @CallSuper
    override fun taskResume(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "taskResume: ")
        getProvider(appTask.fileExt)?.taskResume(appTask)?: run { UtilKLogWrapper.e(TAG,"taskResume no provider") }
    }

    @CallSuper
    override fun taskCancel(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "taskCancel: ")
        getProvider(appTask.fileExt)?.taskCancel(appTask)?: run { UtilKLogWrapper.e(TAG,"taskCancel no provider") }
    }

    ////////////////////////////////////////////////////////////////////

    @SuppressLint("MissingSuperCall")
    @CallSuper
    override fun onTaskStarted(taskState: Int, appTask: AppTask) {
        getProvider(appTask.fileExt)?.onTaskStarted(taskState, appTask)
    }

    @SuppressLint("MissingSuperCall")
    @CallSuper
    override fun onTaskPaused(taskState: Int, appTask: AppTask) {
        getProvider(appTask.fileExt)?.onTaskPaused(taskState, appTask)
    }

    @SuppressLint("MissingSuperCall")
    @CallSuper
    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        getProvider(appTask.fileExt)?.onTaskFinished(taskState, finishType, appTask)
    }
}