package com.mozhimen.taskk.provider.uninstall

import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderUninstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskProviderSetUninstall
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskProvidersUninstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
class TaskProviderSetUninstall(override val providerDefault: ATaskProviderUninstall) : ATaskProviderSetUninstall {
    override val providers: ConcurrentHashMap<String, ATaskProviderUninstall> by lazy { ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault }) }
}