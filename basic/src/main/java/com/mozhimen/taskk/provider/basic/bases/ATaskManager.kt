package com.mozhimen.taskk.provider.basic.bases

import android.content.Context
import androidx.annotation.CallSuper
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.bases.BaseUtilK
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetDownload
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetInstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetOpen
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUninstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUnzip
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetVerify
import com.mozhimen.taskk.provider.basic.cons.CState
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.interfaces.ITask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskEvent
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.interfaces.ITasks
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @ClassName ITaskProviderSets
 * @Description TODO
 * @Author mozhimen
 * @Date 2024/8/20
 * @Version 1.0
 */
@OApiInit_InApplication
abstract class ATaskManager : BaseUtilK(), ITask, ITaskEvent {
    protected val _isInit = AtomicBoolean(false)
    protected val _taskListeners = mutableListOf<ITasks>()
    protected val _taskSets: ConcurrentHashMap<@ATaskName String, ATaskSet<*>> by lazy {
        ConcurrentHashMap<String, ATaskSet<*>>(
            getTaskSets().associateBy { it.getTaskName() }
        )
    }
    protected val _taskQuenes by lazy {
        ConcurrentHashMap<String, List<@ATaskName String>>(getTaskQueues())
    }

    protected val _iTaskLifecycle: ITaskLifecycle = object : ITaskLifecycle {
        override fun onTaskStarted(taskState: Int, appTask: AppTask) {
            if (appTask.isAnyTasking()) {
                onTask_ofIng(appTask, appTask.taskDownloadProgress, appTask.taskDownloadFileSizeOffset, appTask.taskDownloadFileSizeTotal, appTask.taskDownloadFileSpeed)
            } else {
                onTask_ofOther(appTask)
            }
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

    /////////////////////////////////////////////////////////////////

    @CallSuper
    open fun init(context: Context) {
        if (hasInit()) return
        _isInit.compareAndSet(false, true)
        AppTaskDaoManager.init()
        getTaskSets().forEach {
            it.init(context)
        }
        getTaskProviders().forEach {
            it.init(context)
        }
        UtilKLogWrapper.d(TAG, "init: ")
    }

    fun hasInit(): Boolean =
        _isInit.get().also { UtilKLogWrapper.d(TAG, "hasInit: $it") }

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

    fun getTaskSet(@ATaskName taskName: String): ATaskSet<*>? {
        return _taskSets[taskName].also { UtilKLogWrapper.d(TAG, "getTaskSet: taskName $taskName taskSet $it") }
    }

    fun getNextTaskName(fileExt: String, @ATaskName currentTaskName: String): String? {
        val taskQueue = getTaskQueue(fileExt) ?: return null
        val currentIndex = taskQueue.indexOf(currentTaskName)
        if (currentIndex < 0) return null
        val nextIndex = currentIndex + 1
        return (if (nextIndex in taskQueue.indices) {
            taskQueue[nextIndex]
        } else
            null).also { UtilKLogWrapper.d(TAG, "getNextTaskName: $it") }
    }

    fun getNextTaskSet(fileExt: String, @ATaskName taskName: String): ATaskSet<*>? {
        return getNextTaskName(fileExt, taskName)?.let { getTaskSet(it) }
    }

    fun getTaskQueue(fileExt: String): List<@ATaskName String>? {
        return _taskQuenes[fileExt]
    }

    fun getAppTaskDaoManager(): AppTaskDaoManager =
        AppTaskDaoManager

    /////////////////////////////////////////////////////////////////

    abstract fun getTaskQueues(): Map<String, List<@ATaskName String>>
    abstract fun getTaskSets(): List<ATaskSet<*>>
    abstract fun getTaskProviders(): List<ATaskProvider>

    /////////////////////////////////////////////////////////////////

    @OPermission_INTERNET
    fun getTaskSetDownload(): ATaskSetDownload? {
        return getTaskSet(ATaskName.TASK_DOWNLOAD) as? ATaskSetDownload
    }

    fun getTaskSetVerify(): ATaskSetVerify? {
        return getTaskSet(ATaskName.TASK_VERIFY) as? ATaskSetVerify
    }

    fun getTaskSetUnzip(): ATaskSetUnzip? {
        return getTaskSet(ATaskName.TASK_UNZIP) as? ATaskSetUnzip
    }

    fun getTaskSetInstall(): ATaskSetInstall? {
        return getTaskSet(ATaskName.TASK_INSTALL) as? ATaskSetInstall
    }

    fun getTaskSetOpen(): ATaskSetOpen? {
        return getTaskSet(ATaskName.TASK_OPEN) as? ATaskSetOpen
    }

    fun getTaskSetUninstall(): ATaskSetUninstall? {
        return getTaskSet(ATaskName.TASK_UNINSTALL) as? ATaskSetUninstall
    }

    /////////////////////////////////////////////////////////////////

    override fun taskStart(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "taskStart: appTask $appTask")
        if (appTask.isTaskProcess() && !appTask.isAnyTaskPause() && !appTask.isAnyTaskSuccess()) {
            UtilKLogWrapper.d(TAG, "taskStart: task is process")
            return
        }
        val currentTaskName = appTask.getCurrentTaskName(getTaskQueue(appTask.fileExt)?.getOrNull(0) ?: return) ?: return
        if (appTask.isAnyTaskSuccess()) {
            UtilKLogWrapper.d(TAG, "taskStart: getNextTaskSet")
            getNextTaskSet(appTask.fileExt, currentTaskName)?.taskStart(appTask)
        } else {
            UtilKLogWrapper.d(TAG, "taskStart: getTaskSet")
            getTaskSet(currentTaskName)?.taskStart(appTask)
        }
    }

    override fun taskCancel(appTask: AppTask/*, onCancelBlock: IAB_Listener<Boolean, Int>? = null*/) {
        UtilKLogWrapper.d(TAG, "taskCancel: appTask $appTask")
        if (!appTask.isTaskProcess()) {
            UtilKLogWrapper.d(TAG, "taskCancel: task is not process")
            return
        }
        getTaskSet(appTask.getCurrentTaskName() ?: return)?.taskCancel(appTask)
    }

    override fun taskPause(appTask: AppTask) {
        if (!appTask.isTaskProcess()) {
            UtilKLogWrapper.d(TAG, "taskPause: task is not process")
            return
        }
        if (appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskPause: already pause")
            return
        }
        getTaskSet(appTask.getCurrentTaskName() ?: return)?.taskPause(appTask)
    }

    override fun taskResume(appTask: AppTask) {
        if (!appTask.isTaskProcess()) {
            UtilKLogWrapper.d(TAG, "taskResume: task is not process")
            return
        }
        if (!appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskResume: task is not pause")
            return
        }
        getTaskSet(appTask.getCurrentTaskName() ?: return)?.taskResume(appTask)
    }

    /////////////////////////////////////////////////////////////////

    override fun onTaskCreate(appTask: AppTask, isUpdate: Boolean) {
        appTask.toNewTaskState(if (isUpdate) CState.STATE_TASK_UPDATE else CState.STATE_TASK_CREATE)
        _iTaskLifecycle.onTaskStarted(appTask.taskState, appTask)
    }

    override fun onTaskUnavailable(appTask: AppTask) {
        appTask.toNewTaskState(CState.STATE_TASK_UNAVAILABLE)
        _iTaskLifecycle.onTaskStarted(appTask.taskState, appTask)
    }

    fun onTaskSuccess(appTask: AppTask) {
        onTaskFinish(appTask, STaskFinishType.SUCCESS)
    }

    fun onTaskCancel(appTask: AppTask) {
        onTaskFinish(appTask, STaskFinishType.CANCEL)
    }

    fun onTaskFail(appTask: AppTask, exception: TaskException) {
        onTaskFinish(appTask, STaskFinishType.FAIL(exception))
    }

    override fun onTaskFinish(appTask: AppTask, finishType: STaskFinishType) {
        appTask.toNewTaskState(
            when (finishType) {
                STaskFinishType.SUCCESS -> CState.STATE_TASK_SUCCESS
                STaskFinishType.CANCEL -> CState.STATE_TASK_CANCEL
                is STaskFinishType.FAIL -> CState.STATE_TASK_FAIL
            }
        )
        _iTaskLifecycle.onTaskFinished(appTask.taskState, finishType, appTask)
    }

    /////////////////////////////////////////////////////////////////

    fun onTask_ofFail(appTask: AppTask, exception: TaskException) {
        UtilKLogWrapper.d(TAG, "postState_ofFail: id ${appTask.taskId} state ${appTask.getStrTaskState()} exception ${exception.msg} appTask $appTask")
        for (listener in _taskListeners) {
            when (appTask.taskState) {
                CState.STATE_TASK_FAIL -> listener.onTaskFinish(appTask, STaskFinishType.FAIL(exception))
                CTaskState.STATE_DOWNLOAD_FAIL -> listener.onTaskDownloadFail(appTask, exception)
                CTaskState.STATE_VERIFY_FAIL -> listener.onTaskVerifyFail(appTask, exception)
                CTaskState.STATE_UNZIP_FAIL -> listener.onTaskUnzipFail(appTask, exception)
                CTaskState.STATE_INSTALL_FAIL -> listener.onTaskInstallFail(appTask, exception)
                CTaskState.STATE_OPEN_FAIL -> listener.onTaskOpenFail(appTask, exception)
                CTaskState.STATE_UNINSTALL_FAIL -> listener.onTaskUninstallFail(appTask, exception)
            }
        }
        //
        if (appTask.isTaskFail())
            onTaskCreate(appTask, appTask.taskStateInit == CState.STATE_TASK_UPDATE)
        else if (appTask.isAnyTaskFail())
            onTaskFinish(appTask, STaskFinishType.FAIL(exception))

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
                CState.STATE_TASK_UNAVAILABLE -> listener.onTaskUnavailable(appTask)
                CState.STATE_TASK_SUCCESS -> listener.onTaskFinish(appTask, STaskFinishType.SUCCESS)
                CState.STATE_TASK_CANCEL -> listener.onTaskFinish(appTask, STaskFinishType.CANCEL)
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
        //
        if (appTask.isAnyTaskSuccess()) {
            getNextTaskSet(appTask.fileExt, appTask.getCurrentTaskName() ?: return)?.taskStart(appTask)
        }
        if (appTask.isAnyTaskCancel() && !appTask.isTaskCancel()) {
            onTaskFinish(appTask, STaskFinishType.CANCEL)
        } else if (appTask.isTaskCancel()) {
            onTaskCreate(appTask, appTask.taskStateInit == CState.STATE_TASK_UPDATE)
        }
    }
}