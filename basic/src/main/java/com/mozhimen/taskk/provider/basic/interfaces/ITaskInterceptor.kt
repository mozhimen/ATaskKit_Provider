package com.mozhimen.taskk.provider.basic.interfaces

import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName ITaskProviderInterceptor
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
interface ITaskInterceptor : IUtilK {
    fun isAutoDeleteOrgFiles(): Boolean
    fun deleteOrgFiles(appTask: AppTask)
}