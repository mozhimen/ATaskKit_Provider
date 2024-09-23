package com.mozhimen.taskk.provider.install

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetInstall
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskKProviderInstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
@OptIn(OApiInit_InApplication::class)
open class TaskSetInstall(override val taskManager: ATaskManager, override val providerDefaults: List<ATaskInstall>) : ATaskSetInstall() {
    override val providers: ConcurrentHashMap<String, ATaskInstall> by lazy {
        ConcurrentHashMap<String, ATaskInstall>(
            providerDefaults.mapNotNull { (it.getSupportFileTasks() as? Map<String, ATaskInstall>)?.toMutableMap() }.fold(emptyMap()) { a, n -> a + n })
            .also { UtilKLogWrapper.d(TAG, "providers: $it") }
    }
}