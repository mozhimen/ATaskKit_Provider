package com.mozhimen.taskk.provider.apk.interfaces

import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName ITaskProviderInterceptorApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
interface ITaskProviderInterceptorApk:IUtilK {
    fun isAutoDeleteOrgFiles(): Boolean
    fun deleteOrgFiles(appTask: AppTask)
    fun isAutoInstall(): Boolean
}