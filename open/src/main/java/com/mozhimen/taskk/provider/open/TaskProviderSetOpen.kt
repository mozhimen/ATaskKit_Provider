package com.mozhimen.taskk.provider.open

import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderOpen
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskProviderSetOpen
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName ATaskProviderSetOpen
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/8/21 21:24
 * @Version 1.0
 */
class TaskProviderSetOpen(override val providerDefault: ATaskProviderOpen) : ATaskProviderSetOpen() {
    override val providers: ConcurrentHashMap<String, ATaskProviderOpen> = ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault })

}