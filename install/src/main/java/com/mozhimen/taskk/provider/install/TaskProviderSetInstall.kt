package com.mozhimen.taskk.provider.install

import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderInstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskProviderSetInstall
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskKProviderInstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
open class TaskProviderSetInstall(override val providerDefault: ATaskProviderInstall) : ATaskProviderSetInstall() {
    override val providers: ConcurrentHashMap<String, ATaskProviderInstall> by lazy { ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault }) }
}