package com.mozhimen.taskk.provider.core.bases

import android.content.Context
import android.util.Log
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.utilk.android.util.UtilKLogWrapper
import com.mozhimen.taskk.provider.basic.annors.ATaskName
import com.mozhimen.taskk.provider.basic.bases.ATaskSet
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetDownload
import com.mozhimen.taskk.provider.basic.cons.CState
import com.mozhimen.taskk.provider.basic.cons.CTaskState
import com.mozhimen.taskk.provider.basic.cons.STaskFinishType
import com.mozhimen.taskk.provider.basic.db.AppTask
import com.mozhimen.taskk.provider.basic.db.AppTaskDaoManager
import com.mozhimen.taskk.provider.basic.impls.TaskException
import com.mozhimen.taskk.provider.basic.interfaces.ITask
import com.mozhimen.taskk.provider.basic.interfaces.ITaskEvent
import com.mozhimen.taskk.provider.basic.interfaces.ITaskLifecycle
import com.mozhimen.taskk.provider.basic.bases.ATaskProvider
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetInstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetOpen
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUninstall
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetUnzip
import com.mozhimen.taskk.provider.basic.bases.sets.ATaskSetVerify

/**
 * @ClassName NetKAppDownload
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/10/12 9:38
 * @Version 1.0
 */
@OApiInit_InApplication
abstract class ATaskProviders : ITask, ATaskProvider(), ITaskEvent {

    protected val _iTaskLifecycle: ITaskLifecycle = object : ITaskLifecycle {
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

    /////////////////////////////////////////////////////////////////

    override fun init(context: Context) {
//        _taskProviderInstallApk.setTaskProviderInterceptor(TaskProviderInterceptorApk).setInstallKReceiverProxy(InstallKManager.getInstallKReceiverProxy()).init(context)
        getTaskSets().forEach {
            it.init(context)
        }
        super.init(context)
    }

    override fun getNextTaskSet(taskName: String): ATaskSet<*>? {
        val currentIndex = getTaskQueue().indexOf(taskName)
        if (currentIndex < 0) return null
        val nextIndex = currentIndex + 1
        if (nextIndex in getTaskQueue().indices) {
            val nextTaskName = getTaskQueue()[nextIndex]
            return getTaskSet(nextTaskName)
        } else
            return null
    }

    fun getAppTaskDaoManager(): AppTaskDaoManager =
        AppTaskDaoManager

    /////////////////////////////////////////////////////////////////
    // control
    /////////////////////////////////////////////////////////////////
    //region # control
    override fun taskStart(appTask: AppTask) {
        UtilKLogWrapper.d(TAG, "taskStart: appTask $appTask")
        if (appTask.isTaskProcess()) {
            UtilKLogWrapper.d(TAG, "taskCancel: task is process")
            return
        }
        getTaskSet(appTask.getCurrentTaskName() ?: return)?.taskStart(appTask)
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
            UtilKLogWrapper.d(TAG, "downloadResume: task is not process")
            return
        }
        if (!appTask.isAnyTaskPause()) {
            UtilKLogWrapper.d(TAG, "taskResume: task is not pause")
            return
        }
        getTaskSet(appTask.getCurrentTaskName() ?: return)?.taskResume(appTask)
    }

    ////////////////////////////////////////////////////////////////////

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

    override fun onTaskCreate(appTask: AppTask, isUpdate: Boolean) {
        appTask.toNewTaskState(if (isUpdate) CState.STATE_TASK_UPDATE else CState.STATE_TASK_CREATE)
        _iTaskLifecycle.onTaskStarted(appTask.taskState, appTask)
    }

    override fun onTaskUnavailable(appTask: AppTask) {
        appTask.toNewTaskState(CState.STATE_TASK_UNAVAILABLE)
        _iTaskLifecycle.onTaskStarted(appTask.taskState, appTask)
    }

    override fun onTaskSuccess(appTask: AppTask) {
        appTask.toNewTaskState(CState.STATE_TASK_SUCCESS)
        _iTaskLifecycle.onTaskFinished(appTask.taskState, STaskFinishType.SUCCESS, appTask)
    }

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
        //
        onTaskCreate(appTask, appTask.taskStateInit == CState.STATE_TASK_UPDATE)
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
        //
        if (appTask.isAnyTaskSuccess()) {
            getNextTaskSet(appTask.getCurrentTaskName() ?: return)?.taskStart(appTask)
        }
    }
}