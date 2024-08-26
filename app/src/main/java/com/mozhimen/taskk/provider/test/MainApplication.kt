package com.mozhimen.taskk.provider.test

import com.mozhimen.basick.elemk.android.app.bases.BaseApplication
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.lintk.optins.OApiMultiDex_InApplication
import com.mozhimen.taskk.provider.apk.impls.TaskInterceptorApk

/**
 * @ClassName MainApplication
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/23
 * @Version 1.0
 */
@OptIn(OApiMultiDex_InApplication::class)
class MainApplication : BaseApplication() {
    @OptIn(OApiInit_InApplication::class)
    override fun onCreate() {
        super.onCreate()
        MainTaskManager.apply {
            taskProviderApk.setTaskInterceptor(TaskInterceptorApk)
            init(this@MainApplication)
        }
    }
}