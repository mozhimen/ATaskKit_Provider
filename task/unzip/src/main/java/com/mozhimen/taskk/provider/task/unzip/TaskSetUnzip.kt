package com.mozhimen.taskk.provider.task.unzip

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.taskk.provider.basic.bases.ATaskManagerProvider
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUnzip
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUnzip
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskProviderSetUnzip
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/8/21 21:35
 * @Version 1.0
 */
@OptIn(OApiInit_InApplication::class)
class TaskSetUnzip(taskManager: ATaskManagerProvider, override val providerDefaults: List<ATaskUnzip>) : ATaskSetUnzip(taskManager) {
    override val providers: ConcurrentHashMap<String, ATaskUnzip> by lazy {
        ConcurrentHashMap(
            providerDefaults.mapNotNull { (it.getSupportFileTasks() as? Map<String, ATaskUnzip>?) }.fold(mutableMapOf()) { acc, nex -> acc += nex;acc }
        )
    }
}