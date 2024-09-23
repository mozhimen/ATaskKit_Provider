package com.mozhimen.taskk.provider.apk

import android.content.Context
import android.util.Log
import com.liulishuo.okdownload.core.breakpoint.IBreakpointCompare
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.android.content.UtilKPackage
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.installk.manager.commons.IPackagesChangeListener
import com.mozhimen.taskk.provider.apk.cons.CExt
import com.mozhimen.taskk.provider.apk.impls.TaskDeleteApk
import com.mozhimen.taskk.provider.apk.impls.TaskDownloadOkDownloadApk
import com.mozhimen.taskk.provider.apk.impls.TaskInstallApk
import com.mozhimen.taskk.provider.apk.impls.TaskOpenApk
import com.mozhimen.taskk.provider.apk.impls.TaskUninstallApk
import com.mozhimen.taskk.provider.apk.impls.TaskUnzipApk
import com.mozhimen.taskk.provider.apk.impls.TaskVerifyApk
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.bases.ATaskManager
import com.mozhimen.taskk.provider.basic.bases.ATaskProvider
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskClose
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDelete
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskDownload
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskInstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskOpen
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUninstall
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskUnzip
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskVerify
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle

/**
 * @ClassName TaskProviderSetsApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
@OApiInit_InApplication
open class TaskProviderApk(
    iTaskLifecycle: ITaskLifecycle,
    taskManager: ATaskManager,
) : ATaskProvider(iTaskLifecycle, taskManager) {

    protected var _breakpointCompare: IBreakpointCompare? = null

    fun setBreakpointCompare(breakpointCompare: IBreakpointCompare): TaskProviderApk {
        _breakpointCompare = breakpointCompare
        return this
    }

    protected var _sniffTargetFiles: List<String> = listOf()

    fun setTargetFiles(targetFiles: List<String>): TaskProviderApk {
        _sniffTargetFiles = targetFiles
        return this
    }

    ////////////////////////////////////////////////////////////////////

    @OPermission_INTERNET
    override fun getTaskDownload(): ATaskDownload {
        return TaskDownloadOkDownloadApk(_iTaskLifecycle).apply {
            _breakpointCompare?.let {
                setBreakpointCompare(it)
            }
        }
    }

    override fun getTaskVerify(): ATaskVerify {
        return TaskVerifyApk(_iTaskLifecycle)
    }

    override fun getTaskUnzip(): ATaskUnzip {
        return TaskUnzipApk(_iTaskLifecycle).apply {
            if (_sniffTargetFiles.isNotEmpty()) {
                addTargetFiles(_sniffTargetFiles)
            }
        }
    }

    @OptIn(OApiCall_BindLifecycle::class, OApiInit_ByLazy::class)
    override fun getTaskInstall(): ATaskInstall {
        return TaskInstallApk(_iTaskLifecycle).apply {
            this.setInstallKReceiverProxy(InstallKManager.getInstallKReceiverProxy())
        }
    }

    override fun getTaskOpen(): ATaskOpen {
        return TaskOpenApk(_iTaskLifecycle)
    }

    override fun getTaskClose(): ATaskClose? {
        return null
    }

    override fun getTaskUninstall(): ATaskUninstall {
        return TaskUninstallApk(_iTaskLifecycle)
    }

    override fun getTaskDelete(): ATaskDelete? {
        return TaskDeleteApk(_iTaskLifecycle)
    }

    ////////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_InApplication::class)
    override fun init(context: Context) {
        if (hasInit()) return
        super.init(context)
        InstallKManager.apply {
            init(context)
            registerPackagesChangeListener(object : IPackagesChangeListener {
                override fun onPackageAddOrReplace(packageName: String, versionCode: Int) {
                    Log.d(TAG, "onPackageAddOrReplace: packageName $packageName versionCode $versionCode")
                    val appTasks = AppTaskDaoManager.gets_ofApkPackageName_satisfyApkVersionCode(packageName, versionCode)
                    Log.d(TAG, "onPackageAddOrReplace: appTasks $appTasks")
                    appTasks.forEach { appTask ->
                        /*_taskProviderSetInstall*/_taskManager.getTaskSetInstall()?.onTaskFinished(ATaskState.STATE_INSTALL_SUCCESS, appTask, ATaskName.TASK_INSTALL, STaskFinishType.SUCCESS)
                    }
                }

                override fun onPackageRemove(packageName: String) {
                    val appTasks = AppTaskDaoManager.gets_ofApkPackageName(packageName)
                    appTasks.forEach { appTask ->
                        /*_taskProviderSetUninstall*/_taskManager.getTaskSetUninstall()?.onTaskFinished(ATaskState.STATE_UNINSTALL_SUCCESS, appTask, ATaskName.TASK_UNINSTALL, STaskFinishType.SUCCESS)
                    }
                }
            })
        }
        resetSelfState()
    }


    ////////////////////////////////////////////////////////////////////

    override fun getTaskQueue(): Map<String, List<String>> {
        return mapOf(
            ATaskName.TASK_INSTALL to listOf(ATaskName.TASK_DOWNLOAD, ATaskName.TASK_VERIFY, ATaskName.TASK_UNZIP, ATaskName.TASK_INSTALL),
            ATaskName.TASK_OPEN to listOf(ATaskName.TASK_OPEN),
            ATaskName.TASK_UNINSTALL to listOf(ATaskName.TASK_UNINSTALL, ATaskName.TASK_DELETE, ATaskName.TASK_RESTART)
        )
    }

    ////////////////////////////////////////////////////////////////////

    private fun resetSelfState() {
        val packageName = UtilKPackage.getPackageName()
        val appTask = _taskManager.getAppTaskDaoManager().get_ofApkPackageName_ApkVersionCode(packageName, UtilKPackage.getVersionCode(packageName, 0))
        if (appTask != null && appTask.isTaskProcess(_taskManager, ATaskName.TASK_INSTALL)) {
            _taskManager.onTaskFinish(appTask, ATaskName.TASK_INSTALL, STaskFinishType.SUCCESS)
        }
    }
}