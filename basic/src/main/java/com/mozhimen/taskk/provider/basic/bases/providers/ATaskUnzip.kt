package com.mozhimen.taskk.provider.basic.bases.providers

import androidx.annotation.CallSuper
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import java.io.File

/**
 * @ClassName INetKAppUnzipProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
abstract class ATaskUnzip(taskManager: ATaskManager,iTaskLifecycle: ITaskLifecycle?) : ATask(taskManager,iTaskLifecycle) {
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
    override fun taskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        onTaskStarted(ATaskState.STATE_UNZIPING, appTask, taskNodeQueueName)
    }

    override fun taskResume(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
    }

    override fun taskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
    }

    override fun taskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
    }

    override fun canTaskStart(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return true
    }

    override fun canTaskResume(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return false
    }

    override fun canTaskPause(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return false
    }

    override fun canTaskCancel(appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String): Boolean {
        return false
    }
}