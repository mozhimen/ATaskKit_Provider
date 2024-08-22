package com.mozhimen.taskk.provider.unzip

import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderUnzip
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskProviderSetUnzip
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskProviderSetUnzip
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/8/21 21:35
 * @Version 1.0
 */
class TaskProviderSetUnzip(override val providerDefault: ATaskProviderUnzip) : ATaskProviderSetUnzip() {
    override val providers: ConcurrentHashMap<String, ATaskProviderUnzip> = ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault })
}