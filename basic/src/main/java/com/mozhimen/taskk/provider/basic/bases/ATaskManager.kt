package com.mozhimen.taskk.provider.basic.bases

import android.content.Context
import android.util.Log
import androidx.annotation.CallSuper
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_INTERNET
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.bases.BaseUtilK
import com.mozhimen.taskk.provider.basic.annors.AFileExt
import com.mozhimen.taskk.provider.basic.annors.AState
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.annors.ATaskQueueName
import com.mozhimen.taskk.provider.basic.annors.ATaskState
import com.mozhimen.taskk.provider.basic.annors.taskName2taskState
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetDelete
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetDownload
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetInstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetOpen
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUninstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUnzip
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetVerify
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.commons.ITask
import com.mozhimen.taskk.provider.basic.commons.ITaskEvent
import com.mozhimen.taskk.provider.basic.commons.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.commons.ITasks
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

    //一个文件后缀的作用的任务链
    protected val _taskQuenes by lazy {
        ConcurrentHashMap<@AFileExt String, Map<@ATaskQueueName String, List<@ATaskName String>>>(getTaskQueues())
    }

    protected val _iTaskLifecycle: ITaskLifecycle = object : ITaskLifecycle {
        override fun onTaskStarted(taskState: Int, appTask: AppTask, @ATaskQueueName taskQueueName: String) {
            if (appTask.isAnyTasking()) {
                onTask_ofIng(appTask, appTask.taskDownloadProgress, appTask.taskDownloadFileSizeOffset, appTask.taskDownloadFileSizeTotal, appTask.taskDownloadFileSpeed)
            } else {
                onTask_ofOther(appTask, taskQueueName)
            }
        }

        override fun onTaskPaused(taskState: Int, appTask: AppTask, @ATaskQueueName taskQueueName: String) {
            onTask_ofOther(appTask, taskQueueName)
        }

        override fun onTaskFinished(taskState: Int, appTask: AppTask, @ATaskQueueName taskQueueName: String, finishType: STaskFinishType) {
            when (finishType) {
                STaskFinishType.SUCCESS -> onTask_ofOther(appTask, taskQueueName)
                STaskFinishType.CANCEL -> onTask_ofOther(appTask, taskQueueName)
                is STaskFinishType.FAIL -> onTask_ofFail(appTask, taskQueueName, finishType.exception)
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

    fun getAppTaskDaoManager(): AppTaskDaoManager =
        AppTaskDaoManager

    /////////////////////////////////////////////////////////////////

    fun getTaskSet(@ATaskName taskName: String): ATaskSet<*>? {
        return _taskSets[taskName].also { UtilKLogWrapper.d(TAG, "getTaskSet: taskName $taskName taskSet $it") }
    }

//    fun getNextTaskSet(fileExt: String, @ATaskQueueName taskQueueName: String, @ATaskName taskName: String): ATaskSet<*>? {
//        return getNextTaskName(fileExt, taskQueueName, taskName)?.let { getTaskSet(it) }
//    }

    /////////////////////////////////////////////////////////////////

    fun getTaskQueue(@AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): List<@ATaskName String>? {
        return _taskQuenes.get(fileExt)?.get(taskQueueName)
    }

    fun getCurrTaskName_ofTaskQueue(appTask: AppTask, @ATaskQueueName taskQueueName: String, @ATaskName firstTaskName_ofTaskQueue: String = ""): @ATaskName String? {
        return appTask.getCurrentTaskName(this, taskQueueName, firstTaskName_ofTaskQueue)
    }

    fun getNextTaskName_ofTaskQueue(@AFileExt fileExt: String, @ATaskQueueName taskQueueName: String, @ATaskName currTaskName: String): @ATaskName String? {
        val taskQueue = getTaskQueue(fileExt, taskQueueName) ?: return null
        val currentIndex = taskQueue.indexOf(currTaskName)
        if (currentIndex < 0) return null
        val nextIndex = currentIndex + 1
        return (if (nextIndex in taskQueue.indices) {
            taskQueue[nextIndex]
        } else
            null).also { UtilKLogWrapper.d(TAG, "getNextTaskName: $it") }
    }

    fun hasTaskName_ofTaskQueue(@AFileExt fileExt: String, @ATaskQueueName taskQueueName: String, @ATaskName taskName: String): Boolean {
        return getTaskQueue(fileExt, taskQueueName)?.contains(taskName) ?: false
    }

    fun getLastTaskName_ofTaskQueue(@AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): @ATaskName String? {
        return getTaskQueue(fileExt, taskQueueName)?.lastOrNull()
    }

    fun getFirstTaskName_ofTaskQueue(@AFileExt fileExt: String, @ATaskQueueName taskQueueName: String): @ATaskName String? {
        return getTaskQueue(fileExt, taskQueueName)?.firstOrNull()
    }

    fun getPrevTaskName_ofTaskQueue(@AFileExt fileExt: String, @ATaskQueueName taskQueueName: String, @ATaskName currentTaskName: String?): @ATaskName String? {
        currentTaskName ?: run {
            Log.d(TAG, "getPrevTaskName_ofTaskQueue: currentTaskName is null")
            return null
        }
        val indexOf = getTaskQueue(fileExt, taskQueueName)?.indexOf(currentTaskName)
        if (indexOf != null && indexOf != -1) {
            val preIndexOf = indexOf - 1
            return (if (preIndexOf < 0) {
                null
            } else {
                getTaskQueue(fileExt, taskQueueName)?.get(preIndexOf)
            }).also { Log.d(TAG, "getPrevTaskName_ofTaskQueue: $it") }
        } else
            return null
    }

    fun getTaskQueueName_ofTaskName(@AFileExt fileExt: String, @ATaskName taskName: String): @ATaskQueueName String? {
        val taskQueues = _taskQuenes.get(fileExt) ?: return null
        return taskQueues.filter { it.value.contains(taskName) }.keys.firstOrNull()
    }

    /////////////////////////////////////////////////////////////////

    abstract fun getTaskQueues(): Map<String, Map<String, List<@ATaskName String>>>
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

    fun getTaskSetDelete(): ATaskSetDelete? {
        return getTaskSet(ATaskName.TASK_DELETE) as? ATaskSetDelete
    }

    /////////////////////////////////////////////////////////////////

    override fun taskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        if (!canTaskStart(appTask, taskQueueName))
            return
        UtilKLogWrapper.d(TAG, "taskStart: appTask $appTask")
        val currTaskName = getCurrTaskName_ofTaskQueue(appTask, taskQueueName, getFirstTaskName_ofTaskQueue(appTask.fileExt, taskQueueName) ?: return) ?: return
        var nextTaskName = getNextTaskName_ofTaskQueue(appTask.fileExt, taskQueueName, currTaskName)
        UtilKLogWrapper.d(TAG, "taskStart: currTaskName $currTaskName nextTaskName $nextTaskName")
        if (appTask.isAnyTaskSuccess()) {
            UtilKLogWrapper.d(TAG, "taskStart: getNextTaskSet")
            if (nextTaskName != null && nextTaskName != ATaskQueueName.TASK_RESTART) {
//                getNextTaskSet(appTask.fileExt, taskQueueName, currTaskName)?.taskStart(appTask, taskQueueName)
                getTaskSet(nextTaskName)?.taskStart(appTask, taskQueueName)
            } else if (!hasTaskName_ofTaskQueue(appTask.fileExt, taskQueueName, currTaskName)) {
                nextTaskName = getFirstTaskName_ofTaskQueue(appTask.fileExt, taskQueueName)
                nextTaskName?.let {
                    getTaskSet(nextTaskName)?.taskStart(appTask, taskQueueName)
                }
            } else {
                UtilKLogWrapper.d(TAG, "taskStart: getNextTaskSet is null")
            }
        } else {
            UtilKLogWrapper.d(TAG, "taskStart: getTaskSet")
            getTaskSet(currTaskName)?.taskStart(appTask, taskQueueName)
        }
    }

    override fun taskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        if (!canTaskResume(appTask, taskQueueName))
            return
        UtilKLogWrapper.d(TAG, "taskResume: appTask $appTask")
        getTaskSet(getCurrTaskName_ofTaskQueue(appTask, taskQueueName) ?: return)?.taskResume(appTask, taskQueueName)
    }

    override fun taskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        if (!canTaskPause(appTask, taskQueueName))
            return
        UtilKLogWrapper.d(TAG, "taskPause: appTask $appTask")
        getTaskSet(getCurrTaskName_ofTaskQueue(appTask, taskQueueName) ?: return)?.taskPause(appTask, taskQueueName)
    }

    override fun taskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        if (!canTaskCancel(appTask, taskQueueName))
            return
        UtilKLogWrapper.d(TAG, "taskCancel: appTask $appTask")
        getTaskSet(getCurrTaskName_ofTaskQueue(appTask, taskQueueName) ?: return)?.taskCancel(appTask, taskQueueName)
    }

    /////////////////////////////////////////////////////////////////

    override fun canTaskStart(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        if (appTask.isTaskProcess(this, taskQueueName) && !appTask.isAnyTaskPause() && !appTask.isAnyTaskSuccess()) {
            UtilKLogWrapper.d(TAG, "canTaskStart: task is process")
            return false
        }
        if (appTask.isTaskSuccess(this, taskQueueName)) {
            UtilKLogWrapper.d(TAG, "canTaskStart: task is success")
            return false
        }
        return getTaskSet(getFirstTaskName_ofTaskQueue(appTask.fileExt, taskQueueName) ?: return false)?.canTaskStart(appTask, taskQueueName) ?: false
    }

    override fun canTaskResume(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        if (!appTask.isTaskProcess(this, taskQueueName)) {
            UtilKLogWrapper.d(TAG, "taskResume: task is not process")
            return false
        }
        if (!appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskResume: task is not pause")
            return false
        }
        return getTaskSet(getCurrTaskName_ofTaskQueue(appTask, taskQueueName) ?: return false)?.canTaskResume(appTask, taskQueueName) ?: false
    }

    override fun canTaskPause(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        if (!appTask.isTaskProcess(this, taskQueueName)) {
            UtilKLogWrapper.d(TAG, "taskPause: task is not process")
            return false
        }
        if (appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskPause: already pause")
            return false
        }
        return getTaskSet(getCurrTaskName_ofTaskQueue(appTask, taskQueueName) ?: return false)?.canTaskPause(appTask, taskQueueName) ?: false
    }

    override fun canTaskCancel(appTask: AppTask, @ATaskQueueName taskQueueName: String): Boolean {
        if (appTask.isTaskSuccess(this, taskQueueName)) {
            UtilKLogWrapper.d(TAG, "canTaskStart: task is success")
            return false
        }
        if (!appTask.isTaskProcess(this, taskQueueName)) {
            UtilKLogWrapper.d(TAG, "canTaskCancel: task is not process")
            return false
        }
        return getTaskSet(getCurrTaskName_ofTaskQueue(appTask, taskQueueName) ?: return false)?.canTaskCancel(appTask, taskQueueName) ?: false
    }

    /////////////////////////////////////////////////////////////////

    override fun onTaskCreate(appTask: AppTask, @ATaskQueueName taskQueueName: String, isUpdate: Boolean) {
        appTask.toTaskStateNew(if (isUpdate) AState.STATE_TASK_UPDATE else AState.STATE_TASK_CREATE)
        _iTaskLifecycle.onTaskStarted(appTask.taskState, appTask, taskQueueName)
    }

    override fun onTaskUnavailable(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        appTask.toTaskStateNew(AState.STATE_TASK_UNAVAILABLE)
        _iTaskLifecycle.onTaskStarted(appTask.taskState, appTask, taskQueueName)
    }

//    fun onTaskSuccess(appTask: AppTask) {
//        onTaskFinish(appTask, STaskFinishType.SUCCESS)
//    }
//
//    fun onTaskCancel(appTask: AppTask) {
//        onTaskFinish(appTask, STaskFinishType.CANCEL)
//    }
//
//    fun onTaskFail(appTask: AppTask, exception: TaskException) {
//        onTaskFinish(appTask, STaskFinishType.FAIL(exception))
//    }

    override fun onTaskFinish(appTask: AppTask, @ATaskQueueName taskQueueName: String, finishType: STaskFinishType) {
        appTask.toTaskStateNew(
            when (finishType) {
                STaskFinishType.SUCCESS -> getLastTaskName_ofTaskQueue(appTask.fileExt, taskQueueName)?.taskName2taskState()?.plus(AState.STATE_TASK_SUCCESS) ?: AState.STATE_TASK_SUCCESS
                STaskFinishType.CANCEL -> AState.STATE_TASK_CANCEL
                is STaskFinishType.FAIL -> AState.STATE_TASK_FAIL
            }
        )
        _iTaskLifecycle.onTaskFinished(appTask.taskState, appTask, taskQueueName, finishType)
    }

    /////////////////////////////////////////////////////////////////

    fun onTask_ofFail(appTask: AppTask, @ATaskQueueName taskQueueName: String, exception: TaskException) {
        UtilKLogWrapper.d(TAG, "onTask: id ${appTask.id} state ${appTask.getTaskStateStr()} exception ${exception.msg} appTask $appTask")
        for (listener in _taskListeners) {
            when (appTask.taskState) {
                AState.STATE_TASK_FAIL -> listener.onTaskFinish(appTask, taskQueueName, STaskFinishType.FAIL(exception))
                ATaskState.STATE_DOWNLOAD_FAIL -> listener.onTaskDownloadFail(appTask, exception)
                ATaskState.STATE_VERIFY_FAIL -> listener.onTaskVerifyFail(appTask, exception)
                ATaskState.STATE_UNZIP_FAIL -> listener.onTaskUnzipFail(appTask, exception)
                ATaskState.STATE_INSTALL_FAIL -> listener.onTaskInstallFail(appTask, exception)
                ATaskState.STATE_OPEN_FAIL -> listener.onTaskOpenFail(appTask, exception)
                ATaskState.STATE_UNINSTALL_FAIL -> listener.onTaskUninstallFail(appTask, exception)
                ATaskState.STATE_DELETE_FAIL -> listener.onTaskDeleteFail(appTask, exception)
            }
        }
        //
        if (appTask.isTaskFail())
            onTaskCreate(appTask, taskQueueName, appTask.taskStateInit == AState.STATE_TASK_UPDATE)
        else if (appTask.isAnyTaskFail())
            onTaskFinish(appTask, taskQueueName, STaskFinishType.FAIL(exception))
    }

    fun onTask_ofIng(appTask: AppTask, progress: Int, currentIndex: Long, totalIndex: Long, offsetIndexPerSeconds: Long) {
        UtilKLogWrapper.d(
            TAG,
            "onTask: id ${appTask.id} state ${appTask.getTaskStateStr()} progress $progress currentIndex $currentIndex totalIndex $totalIndex offsetIndexPerSeconds $offsetIndexPerSeconds appTask $appTask"
        )
        for (listener in _taskListeners) {
            when (appTask.taskState) {
                ATaskState.STATE_DOWNLOADING -> listener.onTaskDownloading(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_VERIFYING -> listener.onTaskVerifying(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_UNZIPING -> listener.onTaskUnziping(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_INSTALLING -> listener.onTaskInstalling(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_OPENING -> listener.onTaskOpening(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_UNINSTALLING -> listener.onTaskUninstalling(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
                ATaskState.STATE_DELETING -> listener.onTaskDeleting(appTask, progress, currentIndex, totalIndex, offsetIndexPerSeconds)
            }
        }
    }

    fun onTask_ofOther(appTask: AppTask, @ATaskQueueName taskQueueName: String) {
        UtilKLogWrapper.d(TAG, "onTask: id ${appTask.id} state ${appTask.getTaskStateStr()} appTask $appTask")
        for (listener in _taskListeners) {
            when (appTask.taskState) {
                AState.STATE_TASK_CREATE -> listener.onTaskCreate(appTask, taskQueueName, false)
                AState.STATE_TASK_UPDATE -> listener.onTaskCreate(appTask, taskQueueName, true)
                AState.STATE_TASK_UNAVAILABLE -> listener.onTaskUnavailable(appTask, taskQueueName)
                AState.STATE_TASK_SUCCESS -> listener.onTaskFinish(appTask, taskQueueName, STaskFinishType.SUCCESS)
                AState.STATE_TASK_CANCEL -> listener.onTaskFinish(appTask, taskQueueName, STaskFinishType.CANCEL)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_DOWNLOAD_PAUSE -> listener.onTaskDownloadPause(appTask)
                ATaskState.STATE_DOWNLOAD_SUCCESS -> listener.onTaskDownloadSuccess(appTask)
                ATaskState.STATE_DOWNLOAD_CANCEL -> listener.onTaskDownloadCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_VERIFY_PAUSE -> listener.onTaskVerifyPause(appTask)
                ATaskState.STATE_VERIFY_SUCCESS -> listener.onTaskVerifySuccess(appTask)
                ATaskState.STATE_VERIFY_CANCEL -> listener.onTaskVerifyCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_UNZIP_PAUSE -> listener.onTaskUnzipPause(appTask)
                ATaskState.STATE_UNZIP_SUCCESS -> listener.onTaskUnzipSuccess(appTask)
                ATaskState.STATE_UNZIP_CANCEL -> listener.onTaskUnzipCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_INSTALL_PAUSE -> listener.onTaskInstallPause(appTask)
                ATaskState.STATE_INSTALL_SUCCESS -> listener.onTaskInstallSuccess(appTask)
                ATaskState.STATE_INSTALL_CANCEL -> listener.onTaskInstallCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_OPEN_PAUSE -> listener.onTaskInstallPause(appTask)
                ATaskState.STATE_OPEN_SUCCESS -> listener.onTaskInstallSuccess(appTask)
                ATaskState.STATE_OPEN_CANCEL -> listener.onTaskInstallCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_UNINSTALL_PAUSE -> listener.onTaskUninstallPause(appTask)
                ATaskState.STATE_UNINSTALL_SUCCESS -> listener.onTaskUninstallSuccess(appTask)
                ATaskState.STATE_UNINSTALL_CANCEL -> listener.onTaskUninstallCancel(appTask)
                ///////////////////////////////////////////////////////////////////////////////
                ATaskState.STATE_DELETE_PAUSE -> listener.onTaskDeletePause(appTask)
                ATaskState.STATE_DELETE_SUCCESS -> listener.onTaskDeleteSuccess(appTask)
                ATaskState.STATE_DELETE_CANCEL -> listener.onTaskDeleteCancel(appTask)
            }
        }

        //next
        if (appTask.isAnyTaskSuccess()) {
            val currTaskName = getCurrTaskName_ofTaskQueue(appTask, taskQueueName) ?: return
            val nextTaskName = getNextTaskName_ofTaskQueue(appTask.fileExt, taskQueueName, currTaskName)
            if (nextTaskName != null && nextTaskName != ATaskQueueName.TASK_RESTART) {
                getTaskSet(nextTaskName)?.taskStart(appTask, taskQueueName)
            } else if (nextTaskName == ATaskQueueName.TASK_RESTART) {
                onTaskCreate(appTask, taskQueueName, appTask.taskStateInit == AState.STATE_TASK_UPDATE)
            }
        }
        if (appTask.isAnyTaskCancel() && !appTask.isTaskCancel()) {
            onTaskFinish(appTask, taskQueueName, STaskFinishType.CANCEL)
        } else if (appTask.isTaskCancel()) {
            onTaskCreate(appTask, taskQueueName, appTask.taskStateInit == AState.STATE_TASK_UPDATE)
        }
    }
}