package com.mozhimen.taskk.provider.uninstall

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
class TaskSetUninstall(override val providerDefault: ATaskUninstall) : ATaskSetUninstall() {
    override val providers: ConcurrentHashMap<String, ATaskUninstall> by lazy { ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault }) }
}