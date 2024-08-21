package com.mozhimen.taskk.provider.basic.bases.providers

import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskProvider
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderLifecycle

/**
 * @ClassName ITaskProviderVerify
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
abstract class ATaskProviderVerify(iTaskProviderLifecycle: ITaskProviderLifecycle?) : ATaskProvider(iTaskProviderLifecycle) {
    override fun getTaskName(): String {
        return ATaskName.TASK_VERIFY
    }
}