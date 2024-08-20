package com.mozhimen.taskk.task.provider.tradition.impls.install

import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optins.OApiInit_ByLazy
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.basick.utilk.commons.IUtilK
import com.mozhimen.basick.utilk.kotlin.collections.ifNotEmptyOr
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.netk.app.NetKApp
import com.mozhimen.taskk.task.provider.apk.impls.TaskProviderInstallApk
import com.mozhimen.taskk.task.provider.cons.CErrorCode
import com.mozhimen.taskk.task.provider.db.AppTask
import com.mozhimen.taskk.task.provider.db.AppTaskDaoManager
import com.mozhimen.taskk.task.provider.commons.providers.ITaskProviderInstall
import com.mozhimen.taskk.task.provider.impls.intErrorCode2taskException
import com.mozhimen.taskk.task.provider.tradition.impls.NetKAppTaskManager
import com.mozhimen.taskk.task.provider.utils.TaskProviderUtil
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName AppInstallManager
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/8 17:21
 * @Version 1.0
 */
@OApiInit_InApplication
internal object NetKAppInstallManager : IUtilK {
    private val _installProviders: ConcurrentHashMap<String, ITaskProviderInstall> = ConcurrentHashMap()

    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
    fun init() {
        _installProviders["apk"] = TaskProviderInstallApk()
    }

    @JvmStatic
    fun addInstallProvider(provider: ITaskProviderInstall) {
        provider.getSupportFileExtensions().forEach { extension ->
            if (!_installProviders.containsKey(extension)) {
                _installProviders[extension] = provider
            }
        }
    }

    @OptIn(OApiCall_BindLifecycle::class, OApiInit_ByLazy::class, OPermission_REQUEST_INSTALL_PACKAGES::class)
    @JvmStatic
    fun install(appTask: AppTask, fileApk: File) {
        if (!appTask.canTaskInstall()) {
            UtilKLogWrapper.e(TAG, "install: the task hasn't unzip or verify success")
            /**
             * Net
             */
            NetKApp.instance.onInstallFail(appTask, CErrorCode.CODE_TASK_INSTALL_HAST_VERIFY_OR_UNZIP.intErrorCode2taskException())
            return
        }
//        if (appTask.isTaskInstall()) {
//            UtilKLogWrapper.d(TAG, "install: the task already installing")
//            return
//        }
//        /**
//         * [CNetKAppState.STATE_INSTALLING]
//         */
//        NetKApp.instance.onInstalling(appTask)

        NetKApp.instance.netKAppInstallProxy.setAppTask(appTask)

        _installProviders.get(fileApk.extension)?.taskStart(appTask)
    }

    @JvmStatic
    fun onInstallSuccess(apkPackageName: String, versionCode: Int) {
        val list = AppTaskDaoManager.gets_ofApkPackageName(apkPackageName)
        list.ifNotEmptyOr({
            it.forEach { appTask ->
                if (appTask.apkVersionCode <= versionCode) {
                    UtilKLogWrapper.d(TAG, "onInstallSuccess: apkPackageName $apkPackageName")
                    onInstallSuccess(appTask)
                }
            }
        }, {
            UtilKLogWrapper.d(TAG, "onInstallSuccess: addPackage $apkPackageName")
            InstallKManager.addPackage(apkPackageName, versionCode)
        })
    }


    @JvmStatic
    fun onInstallSuccess(appTask: AppTask) {
        InstallKManager.addPackage(appTask.apkPackageName, appTask.apkVersionCode)

        if (NetKAppTaskManager.isDeleteApkFile) {
            TaskProviderUtil.deleteFileApk(appTask)
        }

        //将安装状态发给后端
        /*            GlobalScope.launch(Dispatchers.IO) {
                        ApplicationService.install(appDownloadParam0.appId)
                    }*/
        //如果设置自动删除安装包，安装成功后删除安装包
        /*if (AutoDeleteApkSettingHelper.isAutoDelete()) {
                        if (deleteApkFile(appDownloadParam0)) {
                            HandlerHelper.post {
                                AlertTools.showToast("文件已经删除！")
                            }
                        }
                    }*/

        /**
         * [CNetKAppState.STATE_INSTALL_SUCCESS]
         */
        NetKApp.instance.onInstallSuccess(appTask)
    }

    /////////////////////////////////////////////////////

    @JvmStatic
    fun installCancel(appTask: AppTask) {
        TaskProviderUtil.deleteFileApk(appTask)

        /**
         * [CNetKAppState.STATE_INSTALL_CANCEL]
         */
        NetKApp.instance.onInstallCancel(appTask)
    }

    /////////////////////////////////////////////////////

//    @JvmStatic
//    fun onInstallSuccess(apkPackageName: String) {
//        val list = AppTaskDaoManager.getAppTasksByApkPackageName(apkPackageName)
//        list.ifNotEmptyOr({
//            it.forEach { appTask ->
//                UtilKLogWrapper.d(TAG, "onInstallSuccess: apkPackageName $apkPackageName")
//                onInstallSuccess(appTask)
//            }
//        }, {
//            UtilKLogWrapper.d(TAG, "onInstallSuccess: addPackage $apkPackageName")
//            InstallKManager.addPackage(apkPackageName)
//        })
//    }
}