package com.mozhimen.taskk.provider.basic.bases

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.CallSuper
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
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
        providers[appTask.fileExt]?.taskStart(appTask) ?: run { UtilKLogWrapper.e(TAG,"taskStart no provider") }
    }

    @CallSuper
    override fun taskPause(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "taskPause: ")
        providers[appTask.fileExt]?.taskPause(appTask)?: run { UtilKLogWrapper.e(TAG,"taskPause no provider") }
    }

    @CallSuper
    override fun taskResume(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "taskResume: ")
        providers[appTask.fileExt]?.taskResume(appTask)?: run { UtilKLogWrapper.e(TAG,"taskResume no provider") }
    }

    @CallSuper
    override fun taskCancel(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "taskCancel: ")
        providers[appTask.fileExt]?.taskCancel(appTask)?: run { UtilKLogWrapper.e(TAG,"taskCancel no provider") }
    }

    ////////////////////////////////////////////////////////////////////

    @SuppressLint("MissingSuperCall")
    @CallSuper
    override fun onTaskStarted(taskState: Int, appTask: AppTask) {
        providers[appTask.fileExt]?.onTaskStarted(taskState, appTask)
    }

    @SuppressLint("MissingSuperCall")
    @CallSuper
    override fun onTaskPaused(taskState: Int, appTask: AppTask) {
        providers[appTask.fileExt]?.onTaskPaused(taskState, appTask)
    }

    @SuppressLint("MissingSuperCall")
    @CallSuper
    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        providers[appTask.fileExt]?.onTaskFinished(taskState, finishType, appTask)
    }
}