package com.mozhimen.taskk.provider.basic.bases.providers

import androidx.annotation.CallSuper
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle
import java.io.File

/**
 * @ClassName INetKAppUnzipProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
abstract class ATaskUnzip(iTaskLifecycle: ITaskLifecycle?) : ATask(iTaskLifecycle) {
    protected abstract var _unzipDir: File?

    @Volatile
    protected open var _sniffTargetFiles: MutableList<String> = mutableListOf()

    //////////////////////////////////////////////////////////////////

    fun addTargetFile(targetFile: String): ATaskUnzip {
        if (!_sniffTargetFiles.contains(targetFile) && targetFile.isNotEmpty())
            _sniffTargetFiles.add(targetFile)
        return this
    }

    fun addTargetFiles(targetFiles: List<String>): ATaskUnzip {
        targetFiles.forEach { targetFile ->
            if (!_sniffTargetFiles.contains(targetFile) && targetFile.isNotEmpty())
                _sniffTargetFiles.add(targetFile)
        }
        return this
    }

    fun removeTargetFile(targetFile: String): ATaskUnzip {
        if (_sniffTargetFiles.contains(targetFile))
            _sniffTargetFiles.remove(targetFile)
        return this
    }

    fun setUnzipDir(unzipDir: File): ATaskUnzip {
        _unzipDir = unzipDir
        return this
    }

    //////////////////////////////////////////////////////////////////

    abstract fun getIgnorePaths(): List<String>

    //////////////////////////////////////////////////////////////////

    override fun getTaskName(): String {
        return ATaskName.TASK_UNZIP
    }

    @CallSuper
    override fun taskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        onTaskStarted(CTaskState.STATE_UNZIPING, appTask)
    }

    override fun taskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
    }

    override fun taskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
    }

    override fun taskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
    }

    override fun canTaskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return true
    }

    override fun canTaskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return false
    }

    override fun canTaskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return false
    }

    override fun canTaskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        return false
    }
}