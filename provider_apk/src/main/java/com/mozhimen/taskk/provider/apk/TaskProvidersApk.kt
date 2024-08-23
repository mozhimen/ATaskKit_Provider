package com.mozhimen.taskk.provider.apk

import android.content.Context
import com.liulishuo.okdownload.core.breakpoint.IBreakpointCompare
import com.mozhimen.basick.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optins.OApiInit_ByLazy
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.installk.manager.commons.IPackagesChangeListener
import com.mozhimen.taskk.provider.apk.impls.TaskDownloadOkDownloadApk
import com.mozhimen.taskk.provider.apk.impls.TaskInstallApk
import com.mozhimen.taskk.provider.apk.impls.TaskOpenApk
import com.mozhimen.taskk.provider.apk.impls.TaskUninstallApk
import com.mozhimen.taskk.provider.apk.impls.TaskUnzipApk
import com.mozhimen.taskk.provider.apk.impls.TaskVerifyApk
import com.mozhimen.taskk.provider.apk.interfaces.ITaskProviderInterceptorApk
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskSet
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.core.bases.ATaskProviders
import com.mozhimen.taskk.provider.download.TaskSetDownload
import com.mozhimen.taskk.provider.install.TaskSetInstall
import com.mozhimen.taskk.provider.open.TaskSetOpen
import com.mozhimen.taskk.provider.uninstall.TaskSetUninstall
import com.mozhimen.taskk.provider.unzip.TaskSetUnzip
import com.mozhimen.taskk.provider.verify.TaskSetVerify

/**
 * @ClassName TaskProviderSetsApk
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
@OApiInit_InApplication
class TaskProvidersApk : ATaskProviders() {
    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    private object INSTANCE {
        val holder = TaskProvidersApk()
    }

    ////////////////////////////////////////////////////////////////////

    private var _breakpointCompare: IBreakpointCompare? = null

    fun setBreakpointCompare(breakpointCompare: IBreakpointCompare): TaskProvidersApk {
        _breakpointCompare = breakpointCompare
        return this
    }

    private var _iTaskProviderInterceptor: ITaskProviderInterceptorApk? = null

    fun setTaskProviderInterceptor(iTaskProviderInterceptor: ITaskProviderInterceptorApk): TaskProvidersApk {
        _iTaskProviderInterceptor = iTaskProviderInterceptor
        return this
    }

    private var _sniffTargetFile: String = ""

    fun setTargetFile(targetFile: String): TaskProvidersApk {
        _sniffTargetFile = targetFile
        return this
    }

    ////////////////////////////////////////////////////////////////////

    private val _taskDownloadOkDownloadApk by lazy {
        TaskDownloadOkDownloadApk(_iTaskLifecycle).apply {
            _breakpointCompare?.let { setBreakpointCompare(it) }
        }
    }

    private val _taskUnzipApk by lazy {
        TaskUnzipApk(_iTaskLifecycle).apply {
            _iTaskProviderInterceptor?.let { setTaskProviderInterceptor(it) }
            if (_sniffTargetFile.isNotEmpty()) setTargetFile(_sniffTargetFile)
        }
    }

    @OptIn(OApiCall_BindLifecycle::class, OApiInit_ByLazy::class)
    private val _taskInstallApk by lazy {
        TaskInstallApk(_iTaskLifecycle).apply {
            _iTaskProviderInterceptor?.let { setTaskProviderInterceptor(it) }
            this.setInstallKReceiverProxy(InstallKManager.getInstallKReceiverProxy())
        }
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
                    val appTasks = AppTaskDaoManager.gets_ofApkPackageName_satisfyApkVersionCode(packageName, versionCode)
                    appTasks.forEach { appTask ->
                        /*_taskProviderSetInstall*/getTaskSetInstall()?.onTaskFinished(CTaskState.STATE_INSTALL_SUCCESS, STaskFinishType.SUCCESS, appTask)
                    }
                }

                override fun onPackageRemove(packageName: String) {
                    val appTasks = AppTaskDaoManager.gets_ofApkPackageName(packageName)
                    appTasks.forEach { appTask ->
                        /*_taskProviderSetUninstall*/getTaskSetUninstall()?.onTaskFinished(CTaskState.STATE_UNINSTALL_SUCCESS, STaskFinishType.SUCCESS, appTask)
                    }
                }
            })
        }
        resetSelfState()
    }

    ////////////////////////////////////////////////////////////////////

    override fun getTaskQueue():List<String> {
        return listOf(ATaskName.TASK_DOWNLOAD, ATaskName.TASK_VERIFY, ATaskName.TASK_UNZIP, ATaskName.TASK_INSTALL)
    }

    override fun getTaskSets(): List<ATaskSet<*>> {
        return listOf(
            TaskSetDownload(_taskDownloadOkDownloadApk),
            TaskSetVerify(TaskVerifyApk(_iTaskLifecycle)),
            TaskSetUnzip(_taskUnzipApk),
            TaskSetInstall(_taskInstallApk),
            TaskSetOpen(TaskOpenApk(_iTaskLifecycle)),
            TaskSetUninstall(TaskUninstallApk(_iTaskLifecycle))
        )
    }

    ////////////////////////////////////////////////////////////////////

    private fun resetSelfState() {
        val packageName = UtilKPackage.getPackageName()
        val appTask = getAppTaskDaoManager().get_ofApkPackageName_ApkVersionCode(packageName, UtilKPackage.getVersionCode(packageName, 0))
        if (appTask != null && appTask.isTaskProcess()) {
            onTaskSuccess(appTask)
        }
    }
}