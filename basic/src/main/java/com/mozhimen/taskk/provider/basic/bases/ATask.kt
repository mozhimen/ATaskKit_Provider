package com.mozhimen.taskk.provider.basic.bases

import android.content.Context
import androidx.annotation.CallSuper
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.taskk.provider.basic.interfaces.ITaskEvent
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle
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
abstract class ATask(private val _iTaskLifecycle: ITaskLifecycle?) : IUtilK, ITaskLifecycle, ITaskEvent {
    val isInit: AtomicBoolean = AtomicBoolean(false)

    //////////////////////////////////////////////////////

    fun hasInit(): Boolean = isInit.get()

    @CallSuper
    open fun init(context: Context) {
        isInit.compareAndSet(false, true)
    }

    //////////////////////////////////////////////////////

    abstract fun getTaskName(): String
    abstract fun getSupportFileExtensions(): List<String>

    //////////////////////////////////////////////////////

    @CallSuper
    override fun onTaskStarted(taskState: Int, appTask: AppTask) {
        appTask.toNewTaskState(taskState)
        _iTaskLifecycle?.onTaskStarted(taskState, appTask)
    }

    @CallSuper
    override fun onTaskPaused(taskState: Int, appTask: AppTask) {
        appTask.toNewTaskState(taskState)
        _iTaskLifecycle?.onTaskPaused(taskState, appTask)
    }

    @CallSuper
    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        appTask.toNewTaskState(taskState)
        _iTaskLifecycle?.onTaskFinished(taskState, finishType, appTask)
    }
}