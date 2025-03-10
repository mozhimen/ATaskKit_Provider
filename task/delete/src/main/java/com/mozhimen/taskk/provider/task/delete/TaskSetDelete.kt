package com.mozhimen.taskk.provider.task.delete

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDelete
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetDelete
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskKProviderInstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
@OptIn(OApiInit_InApplication::class)
open class TaskSetDelete constructor(taskManager: ATaskManagerProvider, override val providerDefaults: List<ATaskDelete>) : ATaskSetDelete(taskManager) {
    override val providers: ConcurrentHashMap<String, ATaskDelete> by lazy {
        ConcurrentHashMap<String, ATaskDelete>(
            providerDefaults.mapNotNull { (it.getSupportFileTasks() as? Map<String, ATaskDelete>?) }.fold(mutableMapOf()) { acc, nex -> acc += nex;acc })
            .also { UtilKLogWrapper.d(TAG, "providers: $it") }
    }
}