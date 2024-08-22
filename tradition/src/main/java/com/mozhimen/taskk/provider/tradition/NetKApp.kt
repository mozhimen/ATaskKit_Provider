package com.mozhimen.taskk.provider.tradition

import android.content.Context
import androidx.annotation.AnyThread
import androidx.lifecycle.ProcessLifecycleOwner
import com.liulishuo.okdownload.core.breakpoint.IBreakpointCompare
import com.liulishuo.okdownload.core.exception.ServerCanceledException
import com.mozhimen.basick.elemk.commons.I_Listener
import com.mozhimen.basick.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optins.OApiInit_ByLazy
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.lintk.optins.permission.OPermission_REQUEST_INSTALL_PACKAGES
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.basick.utilk.bases.BaseUtilK
import com.mozhimen.basick.utilk.java.io.UtilKFileDir
import com.mozhimen.basick.utilk.kotlin.strFilePath2file
import com.mozhimen.basick.utilk.wrapper.UtilKPermission
import com.mozhimen.installk.manager.InstallKManager
import com.mozhimen.installk.manager.commons.IPackagesChangeListener
import com.mozhimen.postk.livedata.PostKLiveData
import com.mozhimen.taskk.provider.basic.bases.providers.ATaskProviderInstall
import com.mozhimen.taskk.provider.basic.cons.CErrorCode
import com.mozhimen.taskk.provider.basic.cons.CState
import com.mozhimen.taskk.provider.basic.cons.CTaskEvent
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.basic.db.AppTaskDb
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.impls.intErrorCode2taskException
import com.mozhimen.taskk.provider.tradition.impls.NetKAppTaskManager
import com.mozhimen.taskk.provider.core.impls.download.NetKAppDownloadManager
import com.mozhimen.taskk.provider.core.impls.install.NetKAppInstallManager
import com.mozhimen.taskk.provider.tradition.impls.install.NetKAppInstallProxy
import com.mozhimen.taskk.provider.tradition.impls.uninstall.NetKAppUnInstallManager
import com.mozhimen.taskk.provider.core.impls.unzip.NetKAppUnzipManager
import com.mozhimen.taskk.provider.tradition.iterfaces.ITaskState
import com.mozhimen.taskk.provider.tradition.utils.NetKAppUtil
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @ClassName NetKAppDownload
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/10/12 9:38
 * @Version 1.0
 */
@OApiInit_InApplication
class NetKApp : ITaskState, BaseUtilK() {
    private val _appDownloadStateListeners = mutableListOf<ITaskState>()

    @OptIn(OApiCall_BindLifecycle::class, OApiInit_ByLazy::class)
    private val _netKAppInstallProxy by lazy { NetKAppInstallProxy(_context, ProcessLifecycleOwner.get()) }

    private val _isInitNetKApp = AtomicBoolean(false)

    @OptIn(OApiCall_BindLifecycle::class, OApiInit_ByLazy::class)
    val netKAppInstallProxy get() = _netKAppInstallProxy

    /////////////////////////////////////////////////////////////////

    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    private object INSTANCE {
        val holder = NetKApp()
    }

    /////////////////////////////////////////////////////////////////
    // init
    /////////////////////////////////////////////////////////////////
    //region # init
    @OptIn(OApiCall_BindLifecycle::class, OApiInit_ByLazy::class, OPermission_REQUEST_INSTALL_PACKAGES::class)
    fun init(context: Context, compare: IBreakpointCompare, strSourceApkNameUnzip: String = ""): NetKApp {
        if (_isInitNetKApp.compareAndSet(false,true)){
            _netKAppInstallProxy.bindLifecycle(ProcessLifecycleOwner.get())// 注册应用安装的监听 InstalledApkReceiver.registerReceiver(this)
//        NetKOkDownloadExt.init(context)
            AppTaskDb.init(context)
            NetKAppInstallManager.init()
            InstallKManager.apply {
                init(context)
                registerPackagesChangeListener(object : IPackagesChangeListener {
                    override fun onPackageAddOrReplace(packageName: String, versionCode: Int) {
                        NetKAppInstallManager.onInstallSuccess(packageName, versionCode)
                    }

                    override fun onPackageRemove(packageName: String) {
                        NetKAppUnInstallManager.onUninstallSuccess(packageName)
                    }
                })
            }
            NetKAppUnzipManager.init(strSourceApkNameUnzip)
            NetKAppDownloadManager.init(context, compare)
            resetUpdateAppStatus()
        }
        return this
    }

    fun isInit():Boolean =
        _isInitNetKApp.get()

    private fun resetUpdateAppStatus() {
        val appTask = getAppTaskByApkPackageName_VersionCode(UtilKPackage.getPackageName(), UtilKPackage.getVersionCode())
        if (appTask != null && appTask.isTaskProcess()) {
            resetTaskStateOfInstall(appTask)
        }
    }

    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
    fun addInstallProvider(provider: ATaskProviderInstall) {
        NetKAppInstallManager.addInstallProvider(provider)
    }

    fun registerDownloadStateListener(listener: ITaskState) {
        if (!_appDownloadStateListeners.contains(listener)) {
            _appDownloadStateListeners.add(listener)
        }
    }

    fun unregisterDownloadListener(listener: ITaskState) {
        val indexOf = _appDownloadStateListeners.indexOf(listener)
        if (indexOf >= 0)
            _appDownloadStateListeners.removeAt(indexOf)
    }
    //endregion

    /////////////////////////////////////////////////////////////////
    // control
    /////////////////////////////////////////////////////////////////
    //region # control
    fun taskRetry(appTask: AppTask) {
        NetKAppUtil.deleteFileApk(appTask)//删除本地文件.apk + .npk

        if (appTask.taskDownloadUrlCurrent != appTask.taskDownloadUrlInside) {//重新使用内部地址下载
            if (appTask.taskDownloadUrlInside.isNotEmpty()) {
                appTask.taskDownloadUrlCurrent = appTask.taskDownloadUrlInside
                taskStart(appTask)
            } else {
                appTask.taskVerifyEnable = false//重新下载，下次不校验MD5值
                taskStart(appTask)
            }
        }
    }

    fun taskStart(appTask: AppTask) {
        try {
            if (appTask.isTaskProcess() && !appTask.isAnyTaskPause()) {
                UtilKLogWrapper.d(TAG, "taskStart: the task already start")
                return
            }
//            if (NetKAppDownloadManager.getDownloadTaskCount() >= 3) {
//                /**
//                 * [CNetKAppTaskState.STATE_TASK_FAIL]
//                 */
//                onTaskFinish(appTask, ENetKAppFinishType.FAIL(CNetKAppErrorCode.CODE_DOWNLOAD_ENOUGH.intErrorCode2taskException()))
//                return
//            }
            if (InstallKManager.hasPackageName_lessThanInstalledVersionCode(appTask.apkPackageName, appTask.apkVersionCode)) {
                //throw CNetKAppErrorCode.CODE_TASK_HAS_INSTALL.intErrorCode2taskException()
                UtilKLogWrapper.d(TAG, "taskStart: hasPackageNameAndSatisfyVersion")
                /**
                 * [CTaskState.STATE_INSTALL_SUCCESS]
                 */
                onInstallSuccess(appTask)
                return
            }

            if (appTask.taskDownloadFileSizeTotal != 0L) {
                //当前剩余的空间
                val availMemory = UtilKFileDir.External.getFilesRootFreeSpace()
                //需要的最小空间
                val needMinMemory: Long = (appTask.taskDownloadFileSizeTotal * 1.2).toLong()
                //如果当前需要的空间大于剩余空间，提醒清理空间
                if (availMemory < needMinMemory) {
                    throw CErrorCode.CODE_TASK_NEED_MEMORY.intErrorCode2taskException()
                }

                //判断是否为npk,如果是npk,判断空间是否小于需要的2.2倍，如果小于2.2，提示是否继续
                if (appTask.fileNameExt.endsWith(".npk") || (appTask.fileNameExt.endsWith(".apk") && appTask.taskUnzipEnable)) {
                    //警告空间
                    val warningsMemory: Long = (appTask.taskDownloadFileSizeTotal * 2.2).toLong()
                    //如果当前空间小于警告空间，
                    if (availMemory < warningsMemory) {
                        /*                    NiuAlertDialog.Builder(currentActivity)
                                                .setTitle("提示")
                                                .setMessage("存储空间不足，可能会导致安装失败,是否继续下载？")
                                                .setLeftButton("是") { dialog, witch ->
                                                    DownloadManager.download(appTask)
                                                    downloadCallback?.invoke(true)
                                                    dialog.dismiss()
                                                }
                                                .setRightButton("否") { dialog, witch ->
                                                    dialog.dismiss()
                                                }
                                                .show()*/
                        throw CErrorCode.CODE_TASK_NEED_MEMORY.intErrorCode2taskException()
                    }
                }
            }
            AppTaskDaoManager.addOrUpdate(appTask)

            /**
             * [CNetKAppTaskState.STATE_TASK_CREATE]
             */
            onTaskCreate(appTask, appTask.taskState == CState.STATE_TASK_UPDATE)

            NetKAppDownloadManager.download(appTask/*, listener*/)
        } catch (exception: TaskException) {
            /**
             * [CTaskState.STATE_DOWNLOAD_FAIL]
             */
            onDownloadFail(appTask, exception)
        }
    }

    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
    fun taskCancel(appTask: AppTask/*, onCancelBlock: IAB_Listener<Boolean, Int>? = null*/) {
        UtilKLogWrapper.d(TAG, "taskCancel: appTask $appTask")
        if (!appTask.isTaskProcess()) {
            UtilKLogWrapper.d(TAG, "taskCancel: task is not process")
            return
        }
        /*if (*//*appTask.isTaskDownload() && *//*appTask.isTaskWait()) {
            UtilKLogWrapper.d(TAG, "taskCancel: downloadWaitCancel")
            NetKAppDownloadManager.downloadWaitCancel(appTask*//*, onCancelBlock*//*)

        } else*/ if (appTask.atTaskDownload() && appTask.isTaskDownloading() || appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskCancel: downloadCancel")
            NetKAppDownloadManager.downloadCancel(appTask/*, onCancelBlock*/)//从数据库中移除掉
        } else if (appTask.atTaskUnzip() && appTask.isTaskUnzipSuccess()) {
            UtilKLogWrapper.d(TAG, "taskCancel: installCancel")
            NetKAppInstallManager.installCancel(appTask)
        } else if (appTask.atTaskUnzip() && NetKAppUnzipManager.isUnziping(appTask)) {
            UtilKLogWrapper.d(TAG, "taskCancel: CODE_TASK_CANCEL_FAIL_ON_UNZIPING")
//            onCancelBlock?.invoke(false, CNetKAppErrorCode.CODE_TASK_CANCEL_FAIL_ON_UNZIPING)
        } else {
            UtilKLogWrapper.d(TAG, "taskCancel: other")
        }
    }

    fun taskPause(appTask: AppTask) {
        if (!appTask.isTaskProcess()) {
            UtilKLogWrapper.d(TAG, "taskPause: task is not process")
            return
        }
        if (appTask.atTaskDownload() && appTask.isAnyTasking()) {
            UtilKLogWrapper.d(TAG, "taskPause: downloadPause")
            NetKAppDownloadManager.downloadPause(appTask)
        }
    }

    fun taskResume(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "taskResume: ")
        if (!appTask.isTaskProcess()) {
            UtilKLogWrapper.d(TAG, "downloadResume: task is not process")
            return
        }
        if (appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskPause: downloadResume")
            NetKAppDownloadManager.downloadResume(appTask)
        }
    }

    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
    fun taskInstall(appTask: AppTask) {
        NetKAppInstallManager.install(appTask, appTask.filePathNameExt.strFilePath2file())
    }

    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
    fun resetTaskStateOfInstall(appTask: AppTask) {
        NetKAppInstallManager.onInstallSuccess(appTask.apkPackageName, appTask.apkVersionCode)
    }

    fun taskUnzip(appTask: AppTask) {
        if (appTask.filePathNameExt.isNotEmpty()) {
            NetKAppUnzipManager.unzip(appTask)
        }
    }

    fun taskDelete(appTask: AppTask) {
//        AppTaskDaoManager.delete(appTask)
//        NetKAppUnInstallManager.deleteFileApk(appTask)
        taskCancel(appTask)
    }

    fun onDestroy() {
        NetKAppDownloadManager.downloadPauseAll()
    }
    //endregion

    /////////////////////////////////////////////////////////////////
    // state
    /////////////////////////////////////////////////////////////////
    //region # state
    fun generateAppTaskByPackageName(appTask: AppTask): AppTask {
        if (
            getAppTaskByTaskId_PackageName_VersionCode(appTask.taskId, appTask.apkPackageName, appTask.apkVersionCode) == null &&
            InstallKManager.hasPackageName_lessThanInstalledVersionCode(appTask.apkPackageName, appTask.apkVersionCode)
        ) {
            UtilKLogWrapper.d(TAG, "generateAppTaskByPackageName: hasPackageNameAndSatisfyVersion appTask $appTask")
            onInstallSuccess(appTask/*, ENetKAppFinishType.SUCCESS*/)
        } else if (
            (/*appTask.apkIsInstalled ||*/ appTask.taskState == CState.STATE_TASK_SUCCESS) &&
            !InstallKManager.hasPackageName_lessThanInstalledVersionCode(appTask.apkPackageName, appTask.apkVersionCode)
        ) {
            when (appTask.taskState) {
                CState.STATE_TASK_SUCCESS -> {
                    /**
                     * [CState.STATE_TASK_CREATE]
                     */
                    onTaskCreate(appTask, false)
                }

                CState.STATE_TASK_UNAVAILABLE -> {
                    /**
                     * [CState.STATE_TASK_UNAVAILABLE]
                     */
                    onTaskUnavailable(appTask)
                }

                CState.STATE_TASK_UPDATE -> {
                    /**
                     * [CState.STATE_TASK_UPDATE]
                     */
                    onTaskCreate(appTask, true)
                }
            }
        } else if (getAppTaskByTaskId_PackageName_VersionCode(appTask.taskId, appTask.apkPackageName, appTask.apkVersionCode) == null) {
            when (appTask.taskState) {
                CState.STATE_TASK_CREATE -> {
                    /**
                     * [CState.STATE_TASK_CREATE]
                     */
                    onTaskCreate(appTask, false)
                }

                CState.STATE_TASK_UPDATE -> {
                    /**
                     * [CState.STATE_TASK_UPDATE]
                     */
                    onTaskCreate(appTask, true)
                }

                CState.STATE_TASK_UNAVAILABLE -> {
                    /**
                     * [CState.STATE_TASK_UNAVAILABLE]
                     */
                    onTaskUnavailable(appTask)
                }
            }
        }
        return appTask
    }

    /////////////////////////////////////////////////////////////////

    fun getAppTasksIsProcess(): List<AppTask> =
        AppTaskDaoManager.gets_ofIsTaskProcess()

    fun getAppTasksIsInstalled(): List<AppTask> =
        AppTaskDaoManager.gets_ofIsTaskInstallSuccess()

    /////////////////////////////////////////////////////////////////

//    @JvmStatic
//    fun getAppTaskByTaskId_PackageName(taskId: String, packageName: String): AppTask? =
//        AppTaskDaoManager.getByTaskId_PackageName(taskId, packageName)

    fun getAppTaskByTaskId_PackageName_VersionCode(taskId: String, packageName: String, versionCode: Int): AppTask? =
        AppTaskDaoManager.get_ofTaskId_ApkPackageName_ApkVersionCode(taskId, packageName, versionCode)

    /**
     * 通过保存名称获取下载信息
     */
    fun getAppTaskByApkName(apkName: String): AppTask? {
        return AppTaskDaoManager.get_ofFileName(apkName)
    }

//    /**
//     * 通过包名获取下载信息
//     */
//    fun getAppTaskByApkPackageName(apkPackageName: String): AppTask? {
//        return AppTaskDaoManager.getByApkPackageName(apkPackageName)
//    }

    /**
     * 通过包名获取下载信息
     */
    fun getAppTaskByApkPackageName_VersionCode(apkPackageName: String, versionCode: Int): AppTask? {
        return AppTaskDaoManager.get_ofApkPackageName_ApkVersionCode(apkPackageName, versionCode)
    }

    fun getAppTaskByApkPathName(apkPathName: String): AppTask? {
        return AppTaskDaoManager.get_ofFilePathNameExt(apkPathName)
    }

    /////////////////////////////////////////////////////////////////

    /**
     * 是否有正在下载的任务
     */
    @AnyThread
    fun hasDownloading(): Boolean {
        return AppTaskDaoManager.has_ofAtTaskDownload()
    }

    /**
     * 是否有正在校验的任务
     */
    fun hasVerifying(): Boolean {
        return AppTaskDaoManager.has_ofAtTaskVerify()
    }

    /**
     * 是否有正在解压的任务
     */
    fun hasUnziping(): Boolean {
        return AppTaskDaoManager.has_ofAtTaskUnzip()
    }

    /**
     * 判断是否正在下载中
     * @return true 正在下载中  false 当前不是正在下载中
     */
    fun atTaskDownload(appTask: AppTask): Boolean {
        return NetKAppDownloadManager.getAppDownloadProgress(appTask)?.appTask?.atTaskDownload() ?: false//查询下载状态
    }

    fun isDeleteApkFile(isDelete: Boolean) {
        NetKAppTaskManager.isDeleteApkFile = isDelete
    }

    fun isDeleteApkFile(): Boolean =
        NetKAppTaskManager.isDeleteApkFile

    fun getDownloadPath(): File? =
        UtilKFileDir.External.getFilesDownloads()
    //endregion

    /////////////////////////////////////////////////////////////////


    override fun onTaskCreate(appTask: AppTask, isUpdate: Boolean) {
        /*        //将结果传递给服务端
        GlobalScope.launch(Dispatchers.IO) {
            if (appTask.appId == "2") {
                ApplicationService.updateAppDownload("1")
            } else {
                ApplicationService.updateAppDownload(appTask.appId)
            }
        }*/
        applyAppTaskState(appTask, if (isUpdate) CState.STATE_TASK_UPDATE else CState.STATE_TASK_CREATE)
    }

//    override fun onTaskWait(appTask: AppTask) {
//        applyAppTaskState(appTask, CState.STATE_TASK_WAIT)
//    }

    override fun onTasking(appTask: AppTask, state: Int) {
        applyAppTaskState(appTask, state)
    }

    override fun onTaskPause(appTask: AppTask) {
        applyAppTaskState(appTask, CState.STATE_TASK_PAUSE)
    }

    fun onTaskUnavailable(appTask: AppTask) {
        applyAppTaskState(appTask, CState.STATE_TASK_UNAVAILABLE)
    }

//    fun onTaskUpdate(appTask: AppTask) {
//        applyAppTaskState(appTask, CState.STATE_TASK_UPDATE)
//    }

    override fun onTaskFinish(appTask: AppTask, finishType: STaskFinishType) {
        when (finishType) {
            STaskFinishType.SUCCESS ->
                applyAppTaskState(appTask, CState.STATE_TASK_SUCCESS, finishType = finishType)

            STaskFinishType.CANCEL -> {
                appTask.apply {
                    taskDownloadProgress = 0
                    taskDownloadFileSizeOffset = 0
                }
                applyAppTaskState(appTask, CState.STATE_TASK_CANCEL, finishType = finishType, onNext = {
//                    //推送任务取消的指令
                    PostKLiveData.instance.with<String>(CTaskEvent.EVENT_TASK_CANCEL).postValue(appTask.taskId)

                    /**
                     * [CState.STATE_TASK_CREATE]
                     */
                    onTaskCreate(appTask, appTask.taskStateInit == CState.STATE_TASK_UPDATE)
                })
            }

            is STaskFinishType.FAIL -> {
//                appTask.apply {
//                    downloadProgress = 0
//                    downloadFileSize = 0
//                }
                applyAppTaskState(appTask, CState.STATE_TASK_FAIL, finishType = finishType, onNext = {
                    //                    //推送任务失败的指令
                    PostKLiveData.instance.with<String>(CTaskEvent.EVENT_TASK_FAIL).postValue(appTask.taskId)

                    /**
                     * [CState.STATE_TASK_PAUSE]
                     */
                    onTaskPause(appTask)
                })
            }
        }
    }

    /////////////////////////////////////////////////////////////////

//    override fun onDownloadWait(appTask: AppTask) {
//        applyAppTaskState(appTask, CTaskState.STATE_DOWNLOAD_WAIT, onNext = {
//            /**
//             * [CState.STATE_TASK_WAIT]
//             */
//            onTaskWait(appTask)
//        })
//    }

    override fun onDownloading(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {
        applyAppTaskState(appTask, CTaskState.STATE_DOWNLOADING, progress, currentIndex, totalIndex, offsetIndexPerSeconds, onNext = {
            /**
             * [CState.STATE_TASKING]
             */
            onTasking(appTask, CTaskState.STATE_DOWNLOADING)
        })
    }

    override fun onDownloadPause(appTask: AppTask) {
        applyAppTaskState(appTask, CTaskState.STATE_DOWNLOAD_PAUSE, onNext = {
            /**
             * [CState.STATE_TASK_PAUSE]
             */
            onTaskPause(appTask)
        })
    }

    override fun onDownloadCancel(appTask: AppTask) {
        applyAppTaskState(appTask, CTaskState.STATE_DOWNLOAD_CANCEL, onNext = {
            /**
             * [CState.STATE_TASK_CANCEL]
             */
            onTaskFinish(appTask, STaskFinishType.CANCEL)
        })
    }

    override fun onDownloadSuccess(appTask: AppTask) {
        applyAppTaskState(appTask, CTaskState.STATE_DOWNLOAD_SUCCESS, onNext = {
            /**
             * [CState.STATE_TASKING]
             */
            onTasking(appTask, CTaskState.STATE_DOWNLOAD_SUCCESS)
        })
    }

    fun onDownloadFail(appTask: AppTask, exception: Exception?) {
        if (exception is ServerCanceledException) {
            if (exception.responseCode == 404 && appTask.taskDownloadUrlCurrent != appTask.taskDownloadUrlInside && appTask.taskDownloadUrlInside.isNotEmpty()) {
                appTask.taskDownloadUrlCurrent = appTask.taskDownloadUrlInside
                appTask.taskState = CState.STATE_TASK_CREATE
                taskStart(appTask)
            } else {
                /**
                 * [CTaskState.STATE_DOWNLOAD_FAIL]
                 */
                onDownloadFail(appTask, CErrorCode.CODE_TASK_DOWNLOAD_SERVER_CANCELED.intErrorCode2taskException(exception.message ?: ""))
            }
        } else {
            /**
             * [CTaskState.STATE_DOWNLOAD_FAIL]
             */
            onDownloadFail(appTask, CErrorCode.CODE_TASK_DOWNLOAD_SERVER_CANCELED.intErrorCode2taskException())
        }
    }

    override fun onDownloadFail(appTask: AppTask, exception: TaskException) {
//        AppTaskDaoManager.removeAppTaskForDatabase(appTask)

        applyAppTaskStateException(appTask, CTaskState.STATE_DOWNLOAD_FAIL, exception, onNext = {
            /**
             * [CState.STATE_TASK_FAIL]
             */
            onTaskFinish(appTask, STaskFinishType.FAIL(exception))
        })
    }

    /////////////////////////////////////////////////////////////////

    override fun onVerifying(appTask: AppTask) {
        applyAppTaskState(appTask, CTaskState.STATE_VERIFYING, onNext = {
            /**
             * [CState.STATE_TASKING]
             */
            onTasking(appTask, CTaskState.STATE_VERIFYING)
        })
    }

    override fun onVerifySuccess(appTask: AppTask) {
        applyAppTaskState(appTask, CTaskState.STATE_VERIFY_SUCCESS, onNext = {
            /**
             * [CState.STATE_TASKING]
             */
            onTasking(appTask, CTaskState.STATE_VERIFY_SUCCESS)
        })
    }

    override fun onVerifyFail(appTask: AppTask, exception: TaskException) {
        applyAppTaskStateException(appTask, CTaskState.STATE_VERIFY_FAIL, exception, onNext = {
            /**
             * [CState.STATE_TASK_FAIL]
             */
            onTaskFinish(appTask, STaskFinishType.FAIL(exception))
        })
    }

    /////////////////////////////////////////////////////////////////

    override fun onUnziping(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {
        applyAppTaskState(appTask, CTaskState.STATE_UNZIPING, progress, currentIndex, totalIndex, offsetIndexPerSeconds, onNext = {
            /**
             * [CState.STATE_TASKING]
             */
            onTasking(appTask, CTaskState.STATE_UNZIPING)
        })
    }

    @OptIn(OPermission_REQUEST_INSTALL_PACKAGES::class)
    override fun onUnzipSuccess(appTask: AppTask) {
        applyAppTaskState(appTask, CTaskState.STATE_UNZIP_SUCCESS, onNext = {
            /**
             * [CState.STATE_TASKING]
             */
            onTasking(appTask, CTaskState.STATE_UNZIP_SUCCESS)

            if (NetKAppTaskManager.isAutoInstall && UtilKPermission.hasRequestInstallPackages()) {
                taskInstall(appTask)
            }
        })
    }

    override fun onUnzipFail(appTask: AppTask, exception: TaskException) {
        //            AlertTools.showToast("解压失败，请检测存储空间是否足够！")
        applyAppTaskStateException(appTask, CTaskState.STATE_UNZIP_FAIL, exception, onNext = {
            /**
             * [CState.STATE_TASK_FAIL]
             */
            onTaskFinish(appTask, STaskFinishType.FAIL(exception))
        })
    }

    /////////////////////////////////////////////////////////////////

    override fun onInstalling(appTask: AppTask) {
        applyAppTaskState(appTask, CTaskState.STATE_INSTALLING, onNext = {
            /**
             * [CState.STATE_TASKING]
             */
            onTasking(appTask, CTaskState.STATE_INSTALLING)
        })
    }

    override fun onInstallSuccess(appTask: AppTask) {
        applyAppTaskState(appTask, CTaskState.STATE_INSTALL_SUCCESS, onNext = {
            /**
             * [CState.STATE_TASK_SUCCESS]
             */
            onTaskFinish(appTask, STaskFinishType.SUCCESS)
        })
    }

    override fun onInstallFail(appTask: AppTask, exception: TaskException) {
        applyAppTaskState(appTask, CTaskState.STATE_INSTALL_FAIL, onNext = {
            /**
             * [CState.STATE_TASK_FAIL]
             */
            onTaskFinish(appTask, STaskFinishType.FAIL(exception))
        })
    }

    override fun onInstallCancel(appTask: AppTask) {
//        appTask.apply {
//            downloadProgress = 0
//            downloadFileSize = 0
//        }
        applyAppTaskState(appTask, CTaskState.STATE_INSTALL_CANCEL, onNext = {
            /**
             * [CState.STATE_TASK_FAIL]
             */
            onTaskFinish(appTask, STaskFinishType.CANCEL)
        })
    }

    /////////////////////////////////////////////////////////////////

    override fun onUninstallSuccess(appTask: AppTask) {
        applyAppTaskState(appTask, CTaskState.STATE_UNINSTALL_SUCCESS, onNext = {
            /**
             * [CState.STATE_TASK_CANCEL]
             */
            onTaskFinish(appTask, STaskFinishType.CANCEL)
        })//设置为未安装
    }

    /////////////////////////////////////////////////////////////////

    private fun applyAppTaskState(
        appTask: AppTask, state: Int, progress: Int = 0, finishType: STaskFinishType = STaskFinishType.SUCCESS, onNext: I_Listener? = null
    ) {
        appTask.apply {
            this.taskState = state
            if (progress > 0) taskDownloadProgress = progress
        }
        UtilKLogWrapper.d(TAG, "applyAppTaskState: id ${appTask.taskId} state ${appTask.getStrTaskState()} progress ${appTask.taskDownloadProgress} appTask $appTask")
        AppTaskDaoManager.addOrUpdate(appTask)
        postAppTaskState(appTask, state, appTask.taskDownloadProgress, finishType, onNext)
    }

    private fun applyAppTaskState(
        appTask: AppTask, state: Int, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long, onNext: I_Listener? = null
    ) {
        appTask.apply {
            this.taskState = state
            if (progress > 0) taskDownloadProgress = progress
        }
        UtilKLogWrapper.d(
            TAG,
            "applyAppTaskState: id ${appTask.taskId} state ${appTask.getStrTaskState()} progress ${appTask.taskDownloadProgress} currentIndex $currentIndex totalIndex $totalIndex offsetIndexPerSeconds $offsetIndexPerSeconds appTask $appTask"
        )
        AppTaskDaoManager.addOrUpdate(appTask)
        postAppTaskState(appTask, state, appTask.taskDownloadProgress, currentIndex, totalIndex, offsetIndexPerSeconds, onNext)
    }

    private fun applyAppTaskStateException(
        appTask: AppTask, state: Int, exception: TaskException, progress: Int = 0, onNext: I_Listener? = null
    ) {
        appTask.apply {
            this.taskState = state
            if (progress > 0) taskDownloadProgress = progress
        }
        UtilKLogWrapper.d(TAG, "applyAppTaskState: id ${appTask.taskId} state ${appTask.getStrTaskState()} exception $exception appTask $appTask")
        AppTaskDaoManager.addOrUpdate(appTask)
        postAppTaskState(appTask, state, exception, onNext)
    }

    private fun postAppTaskState(appTask: AppTask, state: Int, exception: TaskException, nextMethod: I_Listener?) {
        for (listener in _appDownloadStateListeners) {
            when (state) {
                CTaskState.STATE_DOWNLOAD_FAIL -> listener.onDownloadFail(appTask, exception)
                CTaskState.STATE_VERIFY_FAIL -> listener.onVerifyFail(appTask, exception)
                CTaskState.STATE_UNZIP_FAIL -> listener.onUnzipFail(appTask, exception)
                CTaskState.STATE_INSTALL_FAIL -> listener.onInstallFail(appTask, exception)
            }
        }
        nextMethod?.invoke()
    }

    private fun postAppTaskState(appTask: AppTask, state: Int, progress: Int, finishType: STaskFinishType, nextMethod: I_Listener?) {
        for (listener in _appDownloadStateListeners) {
            when (state) {
                CState.STATE_TASK_CREATE -> listener.onTaskCreate(appTask, false)
                CState.STATE_TASK_UPDATE -> listener.onTaskCreate(appTask, true)
//                CState.STATE_TASK_WAIT -> listener.onTaskWait(appTask)
                CState.STATE_TASKING -> listener.onTasking(appTask, state)
                CState.STATE_TASK_PAUSE -> listener.onTaskPause(appTask)
                CState.STATE_TASK_CANCEL, CState.STATE_TASK_SUCCESS, CState.STATE_TASK_FAIL -> listener.onTaskFinish(appTask, finishType)
                ///////////////////////////////////////////////////////////////////////////////
//                CTaskState.STATE_DOWNLOAD_WAIT -> listener.onDownloadWait(appTask)
                CTaskState.STATE_DOWNLOAD_PAUSE -> listener.onDownloadPause(appTask)
                CTaskState.STATE_DOWNLOAD_CANCEL -> listener.onDownloadCancel(appTask)
                CTaskState.STATE_DOWNLOAD_SUCCESS -> listener.onDownloadSuccess(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                CTaskState.STATE_VERIFYING -> listener.onVerifying(appTask)
                CTaskState.STATE_VERIFY_SUCCESS -> listener.onVerifySuccess(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                CTaskState.STATE_UNZIP_SUCCESS -> listener.onUnzipSuccess(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                CTaskState.STATE_INSTALLING -> listener.onInstalling(appTask)
                CTaskState.STATE_INSTALL_SUCCESS -> listener.onInstallSuccess(appTask)
                CTaskState.STATE_INSTALL_CANCEL -> listener.onInstallCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                CTaskState.STATE_UNINSTALL_SUCCESS -> listener.onUninstallSuccess(appTask)
            }
        }
        nextMethod?.invoke()
    }

    private fun postAppTaskState(appTask: AppTask, state: Int, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long, nextMethod: I_Listener?) {
        for (listener in _appDownloadStateListeners) {
            when (state) {
                CTaskState.STATE_DOWNLOADING -> listener.onDownloading(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                CTaskState.STATE_UNZIPING -> listener.onUnziping(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
            }
        }
        nextMethod?.invoke()
    }
}