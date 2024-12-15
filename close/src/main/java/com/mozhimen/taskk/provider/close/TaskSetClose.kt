package com.mozhimen.taskk.provider.close

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskClose
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetClose
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
class TaskSetClose(taskManager: ATaskManager, override val providerDefaults: List<ATaskClose>) : ATaskSetClose(taskManager) {
    override val providers: ConcurrentHashMap<String, ATaskClose> by lazy {
        ConcurrentHashMap(
            providerDefaults.mapNotNull { (it.getSupportFileTasks() as? Map<String, ATaskClose>?) }.fold(mutableMapOf()) { acc, nex -> acc += nex;acc }
        )
    }
}