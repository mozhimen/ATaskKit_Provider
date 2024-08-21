package com.mozhimen.taskk.provider.apk.interfaces

/**
 * @ClassName ITaskProviderInterceptorApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/21
 * @Version 1.0
 */
interface ITaskProviderInterceptorApk {
    fun isAutoDeleteOrgFiles(): Boolean
    fun deleteOrgFiles()
}