package com.mozhimen.taskk.task.provider.uninstall

import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.kotlin.collections.ifNotEmptyOr
import com.mozhimen.taskk.task.provider.db.AppTaskDaoManager
import com.mozhimen.taskk.task.provider.commons.ITaskProviders
import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderUninstall
import com.mozhimen.taskk.task.provider.db.AppTask
import com.mozhimen.taskk.task.provider.uninstall.impls.TaskProviderUninstallDefault
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskProvidersUninstall
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/19
 * @Version 1.0
 */
object TaskProvidersUninstall : ITaskProviders<ITaskProviderUninstall> {
    override val providerDefault: ITaskProviderUninstall by lazy { TaskProviderUninstallDefault() }
    override val providers: ConcurrentHashMap<String, ITaskProviderUninstall> by lazy { ConcurrentHashMap(providerDefault.getSupportFileExtensions().associateWith { providerDefault }) }

    override fun addProvider(provider: ITaskProviderUninstall) {
        provider.getSupportFileExtensions().forEach { ext ->
            if (!providers.containsKey(ext))
                providers[ext] = provider
        }
    }

    override fun getSupportFileExtensions(): List<String> {
        return providers.keys().toList()
    }

    override fun process(appTask: AppTask) {
        providers[appTask.fileExt]?.process(appTask)
    }

    @JvmStatic
    @OptIn(OApiInit_InApplication::class)
    fun onSuccess(apkPackageName: String) {
        val list = AppTaskDaoManager.getAppTasksByApkPackageName(apkPackageName)
        list.ifNotEmptyOr({
            it.forEach { appTask ->
                UtilKLogWrapper.d(TAG, "onUninstallSuccess: apkPackageName $apkPackageName")
                onUninstallSuccess(appTask)
            }
        }, {
            UtilKLogWrapper.d(TAG, "onUninstallSuccess: removePackage $apkPackageName")
            InstallKManager.removePackage(apkPackageName)
        })
    }


    override fun onSuccess(appTask: AppTask) {
        InstallKManager.removePackage(appTask.apkPackageName)

        /**
         * [CNetKAppState.STATE_UNINSTALL_SUCCESS]
         */
        NetKApp.instance.onUninstallSuccess(appTask)
    }

    override fun onFail(appTask: AppTask) {

    }
}