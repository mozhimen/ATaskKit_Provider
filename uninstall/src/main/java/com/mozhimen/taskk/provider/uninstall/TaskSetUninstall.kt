package com.mozhimen.taskk.provider.uninstall

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUninstall
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskProvidersUninstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
@OptIn(OApiInit_InApplication::class)
class TaskSetUninstall(taskManager: ATaskManager, override val providerDefaults: List<ATaskUninstall>) : ATaskSetUninstall(taskManager) {
    override val providers: ConcurrentHashMap<String, ATaskUninstall> by lazy {
        ConcurrentHashMap(
            providerDefaults.mapNotNull { (it.getSupportFileTasks() as? Map<String, ATaskUninstall>?) }.fold(mutableMapOf()) { acc, nex -> acc += nex;acc }
        )
    }
}