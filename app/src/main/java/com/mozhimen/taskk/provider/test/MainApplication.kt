package com.mozhimen.taskk.provider.test

import com.mozhimen.kotlin.lintk.optins.OApiMultiDex_InApplication
import com.mozhimen.stackk.bases.BaseApplication

/**
 * @ClassName MainApplication
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/23
 * @Version 1.0
 */
@OptIn(OApiMultiDex_InApplication::class)
class MainApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        MainTaskManager.apply {
            init(this@MainApplication)
        }
    }
}