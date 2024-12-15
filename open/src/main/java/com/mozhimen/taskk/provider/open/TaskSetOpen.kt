package com.mozhimen.taskk.provider.open

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.taskk.provider.basic.bases.ATask
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskOpen
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetOpen
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName ATaskProviderSetOpen
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/8/21 21:24
 * @Version 1.0
 */
@OptIn(OApiInit_InApplication::class)
class TaskSetOpen(taskManager: ATaskManager, override val providerDefaults: List<ATaskOpen>) : ATaskSetOpen(taskManager) {
    override val providers: ConcurrentHashMap<String, ATaskOpen> by lazy {
        ConcurrentHashMap(
            providerDefaults.mapNotNull { (it.getSupportFileTasks() as? Map<String, ATaskOpen>?) }.fold(mutableMapOf()) { acc, nex -> acc += nex;acc }
        )
    }
}