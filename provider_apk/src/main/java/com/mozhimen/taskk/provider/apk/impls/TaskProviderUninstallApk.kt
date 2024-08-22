package com.mozhimen.taskk.provider.apk.impls

import android.annotation.SuppressLint
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderLifecycle
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderUninstall
import com.mozhimen.taskk.provider.basic.db.AppTask

/**
 * @ClassName TaskProviderUninstallDefault
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
class TaskProviderUninstallApk(iTaskProviderLifecycle: ITaskProviderLifecycle?) : ATaskProviderUninstall(iTaskProviderLifecycle) {
    override fun getSupportFileExtensions(): List<String> {
        return listOf(CExt.EXT_APK)
    }

    @SuppressLint("MissingSuperCall")
    override fun taskCancel(appTask: AppTask) {

    }

    @SuppressLint("MissingSuperCall")
    override fun taskPause(appTask: AppTask) {
    }

    @SuppressLint("MissingSuperCall")
    override fun taskResume(appTask: AppTask) {
    }
}