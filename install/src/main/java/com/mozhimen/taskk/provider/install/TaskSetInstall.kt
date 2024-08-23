package com.mozhimen.taskk.provider.install

import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetInstall
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskKProviderInstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
open class TaskSetInstall(override val providerDefault: ATaskInstall) : ATaskSetInstall() {
    override val providers: ConcurrentHashMap<String, ATaskInstall> by lazy { ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault }) }
}