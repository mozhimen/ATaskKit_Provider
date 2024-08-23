package com.mozhimen.taskk.provider.unzip

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
class TaskSetUnzip(override val providerDefault: ATaskUnzip) : ATaskSetUnzip() {
    override val providers: ConcurrentHashMap<String, ATaskUnzip> = ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault })
}