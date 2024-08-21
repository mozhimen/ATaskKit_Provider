package com.mozhimen.taskk.provider.basic.bases

import android.content.Context
import androidx.annotation.CallSuper
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderEvent
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderLifecycle
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
abstract class ATaskProvider(private val _iTaskProviderLifecycle: ITaskProviderLifecycle?) : IUtilK, ITaskProviderLifecycle, ITaskProviderEvent {
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
        appTask.taskState = taskState
        _iTaskProviderLifecycle?.onTaskStarted(taskState, appTask)
    }

    @CallSuper
    override fun onTaskPaused(taskState: Int, appTask: AppTask) {
        appTask.taskState = taskState
        _iTaskProviderLifecycle?.onTaskPaused(taskState, appTask)
    }

    @CallSuper
    override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
        appTask.taskState = taskState
        _iTaskProviderLifecycle?.onTaskFinished(taskState, finishType, appTask)
    }
}