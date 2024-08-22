package com.mozhimen.taskk.provider.core

import android.content.Context
import android.util.Log
import com.liulishuo.okdownload.core.breakpoint.IBreakpointCompare
import com.mozhimen.basick.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optins.OApiInit_ByLazy
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.bases.BaseUtilK
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.installk.manager.commons.IPackagesChangeListener
import com.mozhimen.taskk.provider.apk.impls.TaskProviderDownloadOkDownloadApk
import com.mozhimen.taskk.provider.apk.impls.TaskProviderInstallApk
import com.mozhimen.taskk.provider.apk.impls.TaskProviderInterceptorApk
import com.mozhimen.taskk.provider.apk.impls.TaskProviderOpenApk
import com.mozhimen.taskk.provider.apk.impls.TaskProviderUninstallApk
import com.mozhimen.taskk.provider.apk.impls.TaskProviderUnzipApk
import com.mozhimen.taskk.provider.apk.impls.TaskProviderVerifyApk
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskProviderSet
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskProviderSetDownload
import com.mozhimen.taskk.provider.basic.cons.CState
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.interfaces.ITask
import com.mozhimen.taskk.provider.basic.interfaces.ITasks
import com.mozhimen.taskk.provider.basic.interfaces.ITaskProviderLifecycle
import com.mozhimen.taskk.provider.download.TaskProviderSetDownload
import com.mozhimen.taskk.provider.install.TaskProviderSetInstall
import com.mozhimen.taskk.provider.open.TaskProviderSetOpen
import com.mozhimen.taskk.provider.uninstall.TaskProviderSetUninstall
import com.mozhimen.taskk.provider.unzip.TaskProviderSetUnzip
import com.mozhimen.taskk.provider.verify.TaskProviderSetVerify
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @ClassName NetKAppDownload
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/10/12 9:38
 * @Version 1.0
 */
@OApiInit_InApplication
class TaskProvider : BaseUtilK(), ITask {
    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    private object INSTANCE {
        val holder = TaskProvider()
    }

    /////////////////////////////////////////////////////////////////

    private val _taskListeners = mutableListOf<ITasks>()

    private val _isInitNetKApp = AtomicBoolean(false)

    val iTaskProviderLifecycle: ITaskProviderLifecycle = object : ITaskProviderLifecycle {
        override fun onTaskStarted(taskState: Int, appTask: AppTask) {
            onTask_ofIng(appTask, appTask.taskDownloadProgress, appTask.taskDownloadFileSizeOffset, appTask.taskDownloadFileSizeTotal, appTask.taskDownloadFileSpeed)
        }

        override fun onTaskPaused(taskState: Int, appTask: AppTask) {
            onTask_ofOther(appTask)
        }

        override fun onTaskFinished(taskState: Int, finishType: STaskFinishType, appTask: AppTask) {
            when (finishType) {
                STaskFinishType.SUCCESS -> onTask_ofOther(appTask)
                STaskFinishType.CANCEL -> onTask_ofOther(appTask)
                is STaskFinishType.FAIL -> onTask_ofFail(appTask, finishType.exception)
            }
        }
    }

    private val _taskProviderSets = ConcurrentHashMap<String, ATaskProviderSet<*>>()

    //download
    private val _taskProviderDownloadOkDownload = TaskProviderDownloadOkDownloadApk(iTaskProviderLifecycle)
    private val _taskProviderSetDownload = TaskProviderSetDownload(_taskProviderDownloadOkDownload)

    //verify
    private val _taskProviderSetVerify = TaskProviderSetVerify(TaskProviderVerifyApk(iTaskProviderLifecycle))

    //unzip
    private val _taskProviderUnzipApk = TaskProviderUnzipApk(iTaskProviderLifecycle)
    private val _taskProviderSetUnzip = TaskProviderSetUnzip(_taskProviderUnzipApk)

    //install
    private val _taskProviderInstallApk = TaskProviderInstallApk(iTaskProviderLifecycle)
    private val _taskProviderSetInstall = TaskProviderSetInstall(_taskProviderInstallApk)

    //uninstall
    private val _taskProviderSetUninstall = TaskProviderSetUninstall(TaskProviderUninstallApk(iTaskProviderLifecycle))

    //open
    private val _taskProviderSetOpen = TaskProviderSetOpen(TaskProviderOpenApk(iTaskProviderLifecycle))

    /////////////////////////////////////////////////////////////////
    // init
    /////////////////////////////////////////////////////////////////
    //region # init
    @OptIn(OApiCall_BindLifecycle::class, OApiInit_ByLazy::class)
    fun init(context: Context, compare: IBreakpointCompare, strSourceApkNameUnzip: String = ""): TaskProvider {
        if (_isInitNetKApp.compareAndSet(false, true)) {
            InstallKManager.apply {
                init(context)
                registerPackagesChangeListener(object : IPackagesChangeListener {
                    override fun onPackageAddOrReplace(packageName: String, versionCode: Int) {
                        val appTasks = AppTaskDaoManager.gets_ofApkPackageName_satisfyApkVersionCode(packageName, versionCode)
                        appTasks.forEach { appTask ->
                            _taskProviderSetInstall.onTaskFinished(CTaskState.STATE_INSTALL_SUCCESS, STaskFinishType.SUCCESS, appTask)
                        }
                    }

                    override fun onPackageRemove(packageName: String) {
                        val appTasks = AppTaskDaoManager.gets_ofApkPackageName(packageName)
                        appTasks.forEach { appTask ->
                            _taskProviderSetInstall.onTaskFinished(CTaskState.STATE_UNINSTALL_SUCCESS, STaskFinishType.SUCCESS, appTask)
                        }
                    }
                })
            }
            //download
            _taskProviderDownloadOkDownload.setBreakpointCompare(compare).init(context)
            _taskProviderSetDownload.init(context)
            //verify
            _taskProviderSetVerify.init(context)
            //unzip
            _taskProviderUnzipApk.setTaskProviderInterceptor(TaskProviderInterceptorApk).setTargetFile(strSourceApkNameUnzip).init(context)
            _taskProviderSetUnzip.init(context)
            //install
            _taskProviderInstallApk.setTaskProviderInterceptor(TaskProviderInterceptorApk).setInstallKReceiverProxy(InstallKManager.getInstallKReceiverProxy()).init(context)
            _taskProviderSetInstall.init(context)
            //uninstall
            _taskProviderSetUninstall.init(context)
            //open
            _taskProviderSetOpen.init(context)
            //
            resetUpdateAppStatus()
        }
        return this
    }

    fun isInit(): Boolean =
        _isInitNetKApp.get()

    private fun resetUpdateAppStatus() {
        val packageName = UtilKPackage.getPackageName()
        val appTask = getAppTaskDaoManager().get_ofApkPackageName_ApkVersionCode(packageName, UtilKPackage.getVersionCode(packageName, 0))
        if (appTask != null && appTask.isTaskProcess()) {
            _taskProviderSetInstall.onTaskFinished(CTaskState.STATE_INSTALL_SUCCESS, STaskFinishType.SUCCESS, appTask)
        }
    }

    fun registerTaskListener(listener: ITasks) {
        if (!_taskListeners.contains(listener)) {
            _taskListeners.add(listener)
        }
    }

    fun unregisterTaskListener(listener: ITasks) {
        val indexOf = _taskListeners.indexOf(listener)
        if (indexOf >= 0)
            _taskListeners.removeAt(indexOf)
    }

    fun getTaskProviderSet(taskState: Int): ATaskProviderSet<*>? {
        val task: Int = taskState / 10//->哪个环节
        val state: Int = taskState % 10
        Log.d(TAG, "getTaskProviderSet: task $task state $state")
        return when (state) {
            CTaskState.STATE_DOWNLOAD_CREATE / 10 -> _taskProviderSetDownload
            CTaskState.STATE_VERIFY_CREATE / 10 -> _taskProviderSetVerify
            CTaskState.STATE_UNZIP_CREATE / 10 -> _taskProviderSetUnzip
            CTaskState.STATE_INSTALL_CREATE / 10 -> _taskProviderSetInstall
            CTaskState.STATE_OPEN_CREATE / 10 -> _taskProviderSetOpen
            CTaskState.STATE_UNINSTALL_CREATE / 10 -> _taskProviderSetUninstall
            else -> null
        }
    }

    fun getAppTaskDaoManager(): AppTaskDaoManager =
        AppTaskDaoManager
    //endregion

    /////////////////////////////////////////////////////////////////
    // control
    /////////////////////////////////////////////////////////////////
    //region # control
    fun taskStart(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "taskStart: appTask $appTask")
        if (appTask.isTaskProcess()) {
            UtilKLogWrapper.d(TAG, "taskCancel: task is process")
            return
        }
        getTaskProviderSet(appTask.taskState)?.taskStart(appTask)
    }

    fun taskCancel(appTask: AppTask/*, onCancelBlock: IAB_Listener<Boolean, Int>? = null*/) {
        UtilKLogWrapper.d(TAG, "taskCancel: appTask $appTask")
        if (!appTask.isTaskProcess()) {
            UtilKLogWrapper.d(TAG, "taskCancel: task is not process")
            return
        }
        getTaskProviderSet(appTask.taskState)?.taskCancel(appTask)
    }

    fun taskPause(appTask: AppTask) {
        if (!appTask.isTaskProcess()) {
            UtilKLogWrapper.d(TAG, "taskPause: task is not process")
            return
        }
        if (appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskPause: already pause")
            return
        }
        getTaskProviderSet(appTask.taskState)?.taskPause(appTask)
    }

    fun taskResume(appTask: AppTask) {
        if (!appTask.isTaskProcess()) {
            UtilKLogWrapper.d(TAG, "downloadResume: task is not process")
            return
        }
        if (!appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskResume: task is not pause")
            return
        }
        getTaskProviderSet(appTask.taskState)?.taskResume(appTask)
    }

    override fun onTaskCreate(appTask: AppTask, isUpdate: Boolean) {
        appTask.toNewTaskState(if (isUpdate) CState.STATE_TASK_UPDATE else CState.STATE_TASK_CREATE)
        iTaskProviderLifecycle.onTaskStarted(appTask.taskState, appTask)
    }

    override fun onTaskUnavailable(appTask: AppTask) {
        appTask.toNewTaskState(CState.STATE_TASK_UNAVAILABLE)
        iTaskProviderLifecycle.onTaskStarted(appTask.taskState, appTask)
    }

    override fun onTaskSuccess(appTask: AppTask) {
        appTask.toNewTaskState(CState.STATE_TASK_SUCCESS)
        iTaskProviderLifecycle.onTaskFinished(appTask.taskState, STaskFinishType.SUCCESS, appTask)
    }

    /////////////////////////////////////////////////////////////////

    fun taskStart_ofDownload(appTask: AppTask) {
        _taskProviderSets[ATaskName.TASK_DOWNLOAD]?.taskStart(appTask)
    }

    fun taskStart_ofVerify(appTask: AppTask) {
        _taskProviderSets[ATaskName.TASK_VERIFY]?.taskStart(appTask)
    }

    fun taskStart_ofUnzip(appTask: AppTask) {
        _taskProviderSets[ATaskName.TASK_UNZIP]?.taskStart(appTask)
    }

    fun taskStart_ofInstall(appTask: AppTask) {
        _taskProviderSets[ATaskName.TASK_INSTALL]?.taskStart(appTask)
    }

    fun taskStart_ofOpen(appTask: AppTask) {
        _taskProviderSets[ATaskName.TASK_OPEN]?.taskStart(appTask)
    }

    fun taskStart_ofUNINSTALL(appTask: AppTask) {
        _taskProviderSets[ATaskName.TASK_UNINSTALL]?.taskStart(appTask)
    }

    fun taskPauseAll_ofDownload() {
        (_taskProviderSets[ATaskName.TASK_DOWNLOAD] as? ATaskProviderSetDownload)?.taskPauseAll()
    }
    //endregion

    /////////////////////////////////////////////////////////////////

    fun onTask_ofFail(appTask: AppTask, exception: TaskException) {
        UtilKLogWrapper.d(TAG, "postState_ofFail: id ${appTask.taskId} state ${appTask.getStrTaskState()} exception ${exception.msg} appTask $appTask")
        for (listener in _taskListeners) {
            when (appTask.taskState) {
                CTaskState.STATE_DOWNLOAD_FAIL -> listener.onTaskDownloadFail(appTask, exception)
                CTaskState.STATE_VERIFY_FAIL -> listener.onTaskVerifyFail(appTask, exception)
                CTaskState.STATE_UNZIP_FAIL -> listener.onTaskUnzipFail(appTask, exception)
                CTaskState.STATE_INSTALL_FAIL -> listener.onTaskInstallFail(appTask, exception)
                CTaskState.STATE_OPEN_FAIL -> listener.onTaskOpenFail(appTask, exception)
                CTaskState.STATE_UNINSTALL_FAIL -> listener.onTaskUninstallFail(appTask, exception)
            }
        }
    }

    fun onTask_ofIng(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {
        UtilKLogWrapper.d(
            TAG,
            "postState_ofIng: id ${appTask.taskId} state ${appTask.getStrTaskState()} progress $progress currentIndex $currentIndex totalIndex $totalIndex offsetIndexPerSeconds $offsetIndexPerSeconds appTask $appTask"
        )
        for (listener in _taskListeners) {
            when (appTask.taskState) {
                CTaskState.STATE_DOWNLOADING -> listener.onTaskDownloading(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                CTaskState.STATE_VERIFYING -> listener.onTaskVerifying(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                CTaskState.STATE_UNZIPING -> listener.onTaskUnziping(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                CTaskState.STATE_INSTALLING -> listener.onTaskInstalling(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                CTaskState.STATE_OPENING -> listener.onTaskOpening(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                CTaskState.STATE_UNINSTALLING -> listener.onTaskUninstalling(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
            }
        }
    }

    fun onTask_ofOther(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "postState_ofOther: id ${appTask.taskId} state ${appTask.getStrTaskState()} appTask $appTask")
        for (listener in _taskListeners) {
            when (appTask.taskState) {
                CState.STATE_TASK_CREATE -> listener.onTaskCreate(appTask, false)
                CState.STATE_TASK_UPDATE -> listener.onTaskCreate(appTask, true)
                ///////////////////////////////////////////////////////////////////////////////
                CTaskState.STATE_DOWNLOAD_PAUSE -> listener.onTaskDownloadPause(appTask)
                CTaskState.STATE_DOWNLOAD_SUCCESS -> listener.onTaskDownloadSuccess(appTask)
                CTaskState.STATE_DOWNLOAD_CANCEL -> listener.onTaskDownloadCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                CTaskState.STATE_VERIFY_PAUSE -> listener.onTaskVerifyPause(appTask)
                CTaskState.STATE_VERIFY_SUCCESS -> listener.onTaskVerifySuccess(appTask)
                CTaskState.STATE_VERIFY_CANCEL -> listener.onTaskVerifyCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                CTaskState.STATE_UNZIP_PAUSE -> listener.onTaskUnzipPause(appTask)
                CTaskState.STATE_UNZIP_SUCCESS -> listener.onTaskUnzipSuccess(appTask)
                CTaskState.STATE_UNZIP_CANCEL -> listener.onTaskUnzipCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                CTaskState.STATE_INSTALL_PAUSE -> listener.onTaskInstallPause(appTask)
                CTaskState.STATE_INSTALL_SUCCESS -> listener.onTaskInstallSuccess(appTask)
                CTaskState.STATE_INSTALL_CANCEL -> listener.onTaskInstallCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                CTaskState.STATE_OPEN_PAUSE -> listener.onTaskInstallPause(appTask)
                CTaskState.STATE_OPEN_SUCCESS -> listener.onTaskInstallSuccess(appTask)
                CTaskState.STATE_OPEN_CANCEL -> listener.onTaskInstallCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                CTaskState.STATE_UNINSTALL_PAUSE -> listener.onTaskUninstallPause(appTask)
                CTaskState.STATE_UNINSTALL_SUCCESS -> listener.onTaskUninstallSuccess(appTask)
                CTaskState.STATE_UNINSTALL_CANCEL -> listener.onTaskUninstallCancel(appTask)
            }
        }
    }


}