package com.mozhimen.taskk.provider.basic.bases

import android.content.Context
import androidx.annotation.CallSuper
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.taskk.provider.basic.annors.ATaskNodeQueueName
import com.mozhimen.taskk.provider.basic.commons.ITaskEvent
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @ClassName INetKAppProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
/**
 * download(10) -> verify(20) -> unzip(30) -> install(40) -> open(50)
 *     |                                        |             |      ↓
 * delete(90)   <------------------------- uninstall(70) <- close(60)
 */
abstract class ATask(protected val _taskManager: ATaskManagerProvider, protected val _iTaskLifecycle: ITaskLifecycle?) : IUtilK, ITaskLifecycle, ITaskEvent {
    val isInit: AtomicBoolean = AtomicBoolean(false)

    //////////////////////////////////////////////////////

    fun hasInit(): Boolean = isInit.get()

    @CallSuper
    open fun init(context: Context) {
        isInit.compareAndSet(false, true)
    }

    //////////////////////////////////////////////////////

    open fun getSupportFileTasks(): Map<String, ATask> {
        return getSupportFileExts().associateWith { this }
    }

    //////////////////////////////////////////////////////

    abstract fun getTaskName(): String
    abstract fun getSupportFileExts(): List<String>

    //////////////////////////////////////////////////////

    @CallSuper
    override fun onTaskStarted(taskState: Int, appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        appTask.toTaskStateNew(taskState, appTask.taskChannel)
        _iTaskLifecycle?.onTaskStarted(taskState, appTask, taskNodeQueueName)
    }

    @CallSuper
    override fun onTaskPaused(taskState: Int, appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String) {
        appTask.toTaskStateNew(taskState, appTask.taskChannel)
        _iTaskLifecycle?.onTaskPaused(taskState, appTask, taskNodeQueueName)
    }

    @CallSuper
    override fun onTaskFinished(taskState: Int, appTask: AppTask, @ATaskNodeQueueName taskNodeQueueName: String, finishType: STaskFinishType) {
        appTask.toTaskStateNew(taskState, appTask.taskChannel)
        _iTaskLifecycle?.onTaskFinished(taskState, appTask, taskNodeQueueName, finishType)
    }
}