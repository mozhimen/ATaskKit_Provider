package com.mozhimen.taskk.provider.basic.bases

import android.content.Context
import androidx.annotation.CallSuper
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDownload
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskOpen
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUnzip
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskVerify
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @ClassName ATaskProvider
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/23
 * @Version 1.0
 */
@OApiInit_InApplication
abstract class ATaskProvider(
    protected val _iTaskLifecycle: ITaskLifecycle,
    protected val _taskManager: ATaskManager
) {
    protected val _isInit = AtomicBoolean(false)

    @CallSuper
    open fun init(context: Context) {
        _isInit.compareAndSet(false, true)
    }

    fun hasInit(): Boolean =
        _isInit.get()

    @OptIn(OPermission_INTERNET::class)
    abstract fun getTaskDownload(): ATaskDownload
    abstract fun getTaskVerify(): ATaskVerify
    abstract fun getTaskUnzip(): ATaskUnzip
    abstract fun getTaskInstall(): ATaskInstall
    abstract fun getTaskOpen(): ATaskOpen
    abstract fun getTaskUninstall(): ATaskUninstall
    abstract fun getTaskQueue(): List<String>

    @OptIn(OPermission_INTERNET::class)
    fun getSupportFileExtensions(): List<String> {
        val set: Set<String> = listOf(
            getTaskDownload().getSupportFileExts(),
            getTaskVerify().getSupportFileExts(),
            getTaskUnzip().getSupportFileExts(),
            getTaskInstall().getSupportFileExts(),
            getTaskOpen().getSupportFileExts(),
            getTaskUninstall().getSupportFileExts()
        ).fold(emptySet()) { acc, nex -> acc + nex }
        return set.toList()
    }
}

fun main() {
    val list: Set<Int> = listOf(
        listOf(1, 2, 3),
        listOf(2, 2, 2)
    ).fold(emptySet()) { acc, nex -> acc + nex }
    println(list)
}