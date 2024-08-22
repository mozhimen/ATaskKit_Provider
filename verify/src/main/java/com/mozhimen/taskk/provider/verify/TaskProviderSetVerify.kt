package com.mozhimen.taskk.provider.verify

import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderVerify
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskProviderSetVerify
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskProviderSetVerify
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/8/21 21:36
 * @Version 1.0
 */
class TaskProviderSetVerify(override val providerDefault: ATaskProviderVerify) : ATaskProviderSetVerify() {
    override val providers: ConcurrentHashMap<String, ATaskProviderVerify> = ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault })
}