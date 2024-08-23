package com.mozhimen.taskk.provider.open

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
class TaskSetOpen(override val providerDefault: ATaskOpen) : ATaskSetOpen() {
    override val providers: ConcurrentHashMap<String, ATaskOpen> = ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault })

}