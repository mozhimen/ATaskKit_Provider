package com.mozhimen.taskk.provider.basic.bases

import android.content.Context
import androidx.annotation.CallSuper
import com.mozhimen.basick.utilk.bases.BaseUtilK
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.interfaces.ITasks
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @ClassName ITaskProviderSets
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
abstract class ATaskProvider : BaseUtilK() {
    protected val _isInit = AtomicBoolean(false)
    protected val _taskListeners = mutableListOf<ITasks>()
    protected val _taskSets: ConcurrentHashMap<String, ATaskSet<*>> by lazy {
        ConcurrentHashMap<String, ATaskSet<*>>(
            getTaskSets().associateBy { it.getTaskName() }
        )
    }

    @CallSuper
    open fun init(context: Context) {
        _isInit.compareAndSet(false, true)
    }

    fun hasInit(): Boolean =
        _isInit.get()

    fun registerTaskListener(listener: ITasks) {
        if (!_taskListeners.contains(listener)) {
            _taskListeners.add(listener)
        }
    }

    fun unregisterTaskListener(listener: ITasks) {
        val indexOf = _taskListeners.indexOf(listener)
        if (indexOf >= 0)
            _taskListeners.removeAt(indexOf)
    }

    fun getTaskSet(@ATaskName taskName: String): ATaskSet<*>? {
        return _taskSets[taskName]
    }

    abstract fun getTaskQueue(): List<@ATaskName String>
    abstract fun getTaskSets(): List<ATaskSet<*>>
    abstract fun getNextTaskSet(@ATaskName taskName: String): ATaskSet<*>?
}